package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Respuesta del servidor al solicitar un checkout de PayU.
 *
 * <p>El frontend usa estos datos para construir un formulario HTML y
 * redirigir al usuario a la pasarela de PayU.</p>
 *
 * <p>Ejemplo de uso en React:</p>
 * <pre>{@code
 * const form = document.createElement('form');
 * form.method = 'POST';
 * form.action = checkout.actionUrl;
 * Object.entries(checkout.fields).forEach(([k, v]) => {
 *   const input = document.createElement('input');
 *   input.type = 'hidden'; input.name = k; input.value = v;
 *   form.appendChild(input);
 * });
 * document.body.appendChild(form);
 * form.submit();
 * }</pre>
 */
@Data
@Builder
public class CheckoutResponseDTO {

    /**
     * URL del checkout de PayU donde se enviará el formulario.
     * Ejemplo: {@code https://sandbox.checkout.payulatam.com/ppp-web-gateway-payu/}
     */
    private String actionUrl;

    /**
     * Campos del formulario de PayU (clave → valor).
     * Todos los campos deben enviarse como inputs hidden con método POST.
     */
    private Map<String, String> fields;

    /**
     * Referencia única de la transacción generada por el servidor.
     * Formato: {@code ECO-{tenantId}-{PLAN}-{annual|monthly}-{epochSeconds}}
     * Útil para mostrar al usuario o registrar en logs.
     */
    private String referenceCode;

    /** Monto a cobrar en COP (para mostrar al usuario antes de redirigir). */
    private long amountCOP;

    /** Descripción del plan contratado (para UI). */
    private String description;
}
