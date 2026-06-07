package com.demo.ecosalud.model.dto;

import lombok.Data;

/**
 * Solicitud de checkout enviada desde el panel admin.
 *
 * <p>El admin selecciona el plan al que quiere suscribirse y si desea
 * facturación mensual o anual (con descuento del 20%).</p>
 */
@Data
public class CheckoutRequestDTO {

    /**
     * Plan de suscripción solicitado.
     * Valores válidos: {@code STARTER}, {@code PRO}, {@code CLINIC}.
     */
    private String plan;

    /**
     * {@code true} = facturación anual (se cobra el año completo con 20% de descuento).
     * {@code false} = facturación mensual.
     */
    private boolean annual;
}
