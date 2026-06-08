package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.BillingStatusDTO;
import com.demo.ecosalud.model.dto.CheckoutRequestDTO;
import com.demo.ecosalud.model.dto.CheckoutResponseDTO;
import com.demo.ecosalud.multitenancy.TenantContext;
import com.demo.ecosalud.service.BillingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST de facturación y suscripciones.
 *
 * <p>Rutas:</p>
 * <ul>
 *   <li>{@code POST /api/billing/checkout}            — genera parámetros PayU (JWT requerido)</li>
 *   <li>{@code GET  /api/billing/status}              — estado de suscripción actual (JWT requerido)</li>
 *   <li>{@code POST /api/billing/payu/confirmation}   — webhook de PayU (PÚBLICO — sin JWT)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    // ── Checkout ──────────────────────────────────────────────────────────────

    /**
     * Genera los parámetros firmados para el formulario de PayU Webcheckout.
     *
     * <p>El frontend recibe estos campos y los envía a PayU mediante un formulario POST.
     * PayU redirige al usuario a su pasarela de pago.</p>
     *
     * @param request datos del plan y periodicidad seleccionados
     * @return {@link CheckoutResponseDTO} con la URL de acción y los campos del formulario
     */
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.OK)
    public CheckoutResponseDTO checkout(@RequestBody CheckoutRequestDTO request) {
        String tenantSchema = TenantContext.get();
        return billingService.generateCheckout(tenantSchema, request);
    }

    // ── Status ────────────────────────────────────────────────────────────────

    /**
     * Devuelve el estado actual de la suscripción del tenant autenticado.
     *
     * @return {@link BillingStatusDTO} con plan, estado y precios
     */
    @GetMapping("/status")
    public BillingStatusDTO status() {
        String tenantSchema = TenantContext.get();
        return billingService.getStatus(tenantSchema);
    }

    // ── Webhook PayU ──────────────────────────────────────────────────────────

    /**
     * Webhook de confirmación de PayU — PÚBLICO (sin JWT).
     *
     * <p>PayU llama a este endpoint vía POST después de cada intento de pago.
     * El backend verifica la firma MD5 y actualiza el estado del tenant si el
     * pago fue aprobado ({@code state_pol = 4}).</p>
     *
     * <p><b>IMPORTANTE:</b> Este endpoint debe responder siempre con 200 OK (incluso
     * en caso de pago rechazado) para que PayU marque la notificación como recibida.
     * Si responde con error, PayU reintentará la llamada hasta 3 veces.</p>
     *
     * @param httpRequest petición HTTP con los parámetros POST de PayU
     */
    @PostMapping("/payu/confirmation")
    @ResponseStatus(HttpStatus.OK)
    public void payuConfirmation(HttpServletRequest httpRequest) {
        // Leer todos los parámetros del body de PayU como Map<String, String>
        Map<String, String> params = new HashMap<>();
        httpRequest.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        log.info("[Billing/Webhook] POST recibido de PayU — params: {}",
                params.keySet()); // solo keys, no values (pueden tener datos sensibles)

        try {
            billingService.processConfirmation(params);
        } catch (Exception e) {
            // Log pero siempre responder 200 para que PayU no reintente
            log.error("[Billing/Webhook] Error procesando confirmación: {}", e.getMessage(), e);
        }
    }
}
