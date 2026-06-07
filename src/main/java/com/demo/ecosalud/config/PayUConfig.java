package com.demo.ecosalud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuración de la pasarela de pago PayU Colombia.
 *
 * <p>Lee las propiedades desde {@code application.properties} con prefijo {@code payu}.</p>
 *
 * <h3>Variables de entorno para producción (Railway):</h3>
 * <ul>
 *   <li>{@code PAYU_MERCHANT_ID}      — ID de comercio (Dashboard PayU → Mi cuenta)</li>
 *   <li>{@code PAYU_ACCOUNT_ID}       — ID de cuenta Colombia</li>
 *   <li>{@code PAYU_API_KEY}          — Llave API (Dashboard PayU → Integraciones técnicas)</li>
 *   <li>{@code PAYU_SANDBOX}          — {@code false} en producción</li>
 *   <li>{@code PAYU_CONFIRMATION_URL} — URL pública del webhook (HTTPS obligatorio en producción)</li>
 *   <li>{@code PAYU_RESPONSE_URL}     — URL del frontend donde PayU redirige al usuario</li>
 * </ul>
 *
 * <h3>Credenciales de prueba (sandbox oficial PayU):</h3>
 * <pre>
 *   merchantId = 508029
 *   accountId  = 512321
 *   apiKey     = 4Vj8eK4rloUd272L48hsrarnUA
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "payu")
public class PayUConfig {

    /** ID de comercio registrado en PayU. */
    private String merchantId;

    /** ID de cuenta de Colombia en PayU. */
    private String accountId;

    /** Llave API de PayU (usada solo en el servidor para firmas — NUNCA al frontend). */
    private String apiKey;

    /** {@code true} = sandbox; {@code false} = producción. */
    private boolean sandbox = true;

    /**
     * URL donde PayU enviará la confirmación de pago (webhook POST).
     * Debe ser HTTPS y accesible públicamente en producción.
     */
    private String confirmationUrl;

    /**
     * URL de la página de respuesta en el frontend.
     * PayU redirige al usuario aquí después de pagar (éxito, error o cancelación).
     */
    private String responseUrl;

    /** URL del checkout de PayU según el entorno. */
    public String getCheckoutUrl() {
        return sandbox
                ? "https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/"
                : "https://checkout.payulatam.com/ppp-web-gateway-payu/";
    }
}
