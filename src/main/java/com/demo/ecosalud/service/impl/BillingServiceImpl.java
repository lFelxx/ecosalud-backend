package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.config.PayUConfig;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.model.dto.BillingStatusDTO;
import com.demo.ecosalud.model.dto.CheckoutRequestDTO;
import com.demo.ecosalud.model.dto.CheckoutResponseDTO;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementación del servicio de facturación con PayU Colombia.
 *
 * <h3>Flujo de pago PayU Webcheckout:</h3>
 * <ol>
 *   <li>Admin del tenant llama {@code POST /api/billing/checkout}</li>
 *   <li>Backend genera los parámetros firmados del formulario PayU</li>
 *   <li>Frontend crea y envía el formulario → usuario ve la página de pago de PayU</li>
 *   <li>Usuario paga → PayU llama {@code POST /api/billing/payu/confirmation} (webhook)</li>
 *   <li>Backend verifica firma y actualiza el plan/estado del tenant</li>
 *   <li>PayU redirige al usuario a {@code responseUrl} (nuestra página de resultado)</li>
 * </ol>
 *
 * <h3>Precios en COP (IVA incluido, 2025):</h3>
 * <pre>
 *   STARTER  mensual:  $119.000  COP
 *   STARTER  anual:    $1.142.400 COP (=119.000 × 0.80 × 12)
 *   PRO      mensual:  $324.000  COP
 *   PRO      anual:    $3.110.400 COP
 *   CLINIC   mensual:  $817.000  COP
 *   CLINIC   anual:    $7.843.200 COP
 * </pre>
 *
 * <h3>Firma PayU (checkout):</h3>
 * <pre>
 *   MD5("apiKey~merchantId~referenceCode~amount~currency")
 * </pre>
 *
 * <h3>Firma PayU (webhook verificación):</h3>
 * <pre>
 *   MD5("apiKey~merchantId~referenceCode~roundedAmount~currency~transactionState")
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final PayUConfig        payU;
    private final TenantRepository  tenantRepo;

    // ── Tabla de precios en COP ───────────────────────────────────────────────
    // [0] = precio mensual, [1] = precio anual total (con 20% descuento)

    private static final Map<SubscriptionPlan, long[]> PRICES = Map.of(
            SubscriptionPlan.STARTER, new long[]{ 119_000L,   1_142_400L },
            SubscriptionPlan.PRO,     new long[]{ 324_000L,   3_110_400L },
            SubscriptionPlan.CLINIC,  new long[]{ 817_000L,   7_843_200L }
    );

    private static final Map<SubscriptionPlan, String> PLAN_LABELS = Map.of(
            SubscriptionPlan.STARTER, "Plan Starter",
            SubscriptionPlan.PRO,     "Plan Pro",
            SubscriptionPlan.CLINIC,  "Plan Clínica",
            SubscriptionPlan.FOUNDER, "Plan Fundador"
    );

    // ── generateCheckout ──────────────────────────────────────────────────────

    @Override
    public CheckoutResponseDTO generateCheckout(String tenantSchema, CheckoutRequestDTO req) {

        // 1. Validar y parsear el plan solicitado
        SubscriptionPlan plan;
        try {
            plan = SubscriptionPlan.valueOf(req.getPlan().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Plan inválido: " + req.getPlan() + ". Valores válidos: STARTER, PRO, CLINIC");
        }

        if (plan == SubscriptionPlan.FOUNDER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El plan FOUNDER no está disponible para compra.");
        }

        // 2. Obtener datos del tenant
        Tenant tenant = tenantRepo.findBySchemaName(tenantSchema)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tenant no encontrado: " + tenantSchema));

        // 3. Calcular monto
        long[] prices   = PRICES.get(plan);
        long   amount   = req.isAnnual() ? prices[1] : prices[0];
        String period   = req.isAnnual() ? "anual" : "mensual";
        String planLabel = PLAN_LABELS.get(plan);
        String description = planLabel + " Ecosalud Market - " + (req.isAnnual() ? "1 año" : "1 mes");

        // 4. Generar referenceCode único: ECO-{tenantId}-{PLAN}-{annual|monthly}-{epoch}
        String referenceCode = String.format("ECO-%d-%s-%s-%d",
                tenant.getId(), plan.name(), period, Instant.now().getEpochSecond());

        // 5. Calcular firma PayU: MD5("apiKey~merchantId~referenceCode~amount~currency")
        // PayU requiere el monto con exactamente 2 decimales
        String amountFormatted = String.format("%.2f", (double) amount);
        String signatureInput  = payU.getApiKey() + "~"
                + payU.getMerchantId() + "~"
                + referenceCode + "~"
                + amountFormatted + "~"
                + "COP";
        String signature = md5(signatureInput);

        // 6. Construir campos del formulario PayU
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("merchantId",       payU.getMerchantId());
        fields.put("accountId",        payU.getAccountId());
        fields.put("description",      description);
        fields.put("referenceCode",    referenceCode);
        fields.put("amount",           amountFormatted);
        fields.put("tax",              "0");                // IVA separado — simplificado
        fields.put("taxReturnBase",    "0");
        fields.put("currency",         "COP");
        fields.put("signature",        signature);
        fields.put("test",             payU.isSandbox() ? "1" : "0");
        fields.put("buyerEmail",       tenant.getOwnerEmail());
        fields.put("payerFullName",    tenant.getOwnerName());
        fields.put("confirmationUrl",  payU.getConfirmationUrl());
        fields.put("responseUrl",      payU.getResponseUrl());
        fields.put("lng",              "es");               // idioma de la pasarela

        log.info("[Billing] Checkout generado — tenant: {}, plan: {}, período: {}, monto: {} COP, ref: {}",
                tenantSchema, plan, period, amount, referenceCode);

        return CheckoutResponseDTO.builder()
                .actionUrl(payU.getCheckoutUrl())
                .fields(fields)
                .referenceCode(referenceCode)
                .amountCOP(amount)
                .description(description)
                .build();
    }

    // ── processConfirmation ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void processConfirmation(Map<String, String> params) {

        String referenceCode    = params.getOrDefault("reference_sale",   params.get("referenceCode"));
        String statePolStr      = params.getOrDefault("state_pol",        params.get("transactionState"));
        String receivedSign     = params.getOrDefault("sign",             "");
        String amountStr        = params.getOrDefault("value",            "0");
        String currency         = params.getOrDefault("currency",         "COP");
        String merchantId       = params.getOrDefault("merchant_id",      payU.getMerchantId());

        log.info("[Billing/Webhook] Confirmación recibida — ref: {}, state: {}", referenceCode, statePolStr);

        // 1. Verificar firma
        // PayU firma: MD5("apiKey~merchantId~referenceCode~roundedAmount~currency~transactionState")
        // El monto se redondea a 1 decimal
        double amountDouble = 0;
        try { amountDouble = Double.parseDouble(amountStr); } catch (NumberFormatException ignored) {}
        String roundedAmount  = String.format("%.1f", amountDouble);
        String expectedSignIn = payU.getApiKey() + "~"
                + merchantId + "~"
                + referenceCode + "~"
                + roundedAmount + "~"
                + currency + "~"
                + statePolStr;
        String expectedSign = md5(expectedSignIn);

        if (!expectedSign.equalsIgnoreCase(receivedSign)) {
            log.warn("[Billing/Webhook] Firma inválida. Esperada: {}, Recibida: {}", expectedSign, receivedSign);
            return;  // Ignorar — posible intento de fraude o error de config
        }

        // 2. Estado: 4 = APROBADO
        int statePol = 0;
        try { statePol = Integer.parseInt(statePolStr); } catch (NumberFormatException ignored) {}

        if (statePol != 4) {
            log.info("[Billing/Webhook] Pago no aprobado (state_pol={}). No se actualiza el tenant.", statePol);
            return;
        }

        // 3. Parsear referenceCode → ECO-{tenantId}-{PLAN}-{period}-{epoch}
        // Ej: "ECO-3-PRO-mensual-1717718400"
        String[] parts = referenceCode.split("-");
        if (parts.length < 4) {
            log.error("[Billing/Webhook] referenceCode con formato inválido: {}", referenceCode);
            return;
        }
        long tenantId;
        SubscriptionPlan plan;
        try {
            tenantId = Long.parseLong(parts[1]);
            plan     = SubscriptionPlan.valueOf(parts[2]);
        } catch (Exception e) {
            log.error("[Billing/Webhook] No se pudo parsear tenantId/plan de: {}", referenceCode, e);
            return;
        }

        // 4. Actualizar tenant
        tenantRepo.findById(tenantId).ifPresentOrElse(
                tenant -> {
                    tenant.setPlan(plan);
                    tenant.setBillingStatus(BillingStatus.ACTIVE);
                    tenantRepo.save(tenant);
                    log.info("[Billing/Webhook] ✅ Tenant #{} actualizado → plan={}, status=ACTIVE",
                            tenantId, plan);
                },
                () -> log.error("[Billing/Webhook] Tenant #{} no encontrado.", tenantId)
        );
    }

    // ── getStatus ─────────────────────────────────────────────────────────────

    @Override
    public BillingStatusDTO getStatus(String tenantSchema) {
        Tenant tenant = tenantRepo.findBySchemaName(tenantSchema)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tenant no encontrado: " + tenantSchema));

        SubscriptionPlan plan   = tenant.getPlan() != null ? tenant.getPlan() : SubscriptionPlan.STARTER;
        long[] prices           = PRICES.getOrDefault(plan, new long[]{ 0L, 0L });

        return BillingStatusDTO.builder()
                .plan(plan.name())
                .planLabel(PLAN_LABELS.getOrDefault(plan, plan.name()))
                .billingStatus(tenant.getBillingStatus() != null ? tenant.getBillingStatus().name() : "ACTIVE")
                .billingStatusLabel(billingStatusLabel(tenant.getBillingStatus()))
                .accountType(tenant.getAccountType() != null ? tenant.getAccountType().name() : "REGULAR")
                .monthlyPriceCOP(prices[0])
                .annualPriceCOP(prices[1])
                .clinicName(tenant.getName())
                .ownerEmail(tenant.getOwnerEmail())
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String md5(String input) {
        try {
            MessageDigest md     = MessageDigest.getInstance("MD5");
            byte[]        digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb     = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 no disponible en este entorno", e);
        }
    }

    private String billingStatusLabel(BillingStatus status) {
        if (status == null) return "Prueba gratuita";
        return switch (status) {
            case TRIAL     -> "Período de prueba (14 días)";
            case ACTIVE    -> "Al día";
            case OVERDUE   -> "Prueba vencida — actualiza tu plan";
            case PENDING   -> "Pago pendiente";
            case SUSPENDED -> "Suspendido";
            case EXEMPT    -> "Exento (Fundador)";
            case CANCELLED -> "Cancelado";
        };
    }
}
