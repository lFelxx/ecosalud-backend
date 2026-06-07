package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Estado actual de la suscripción de un tenant.
 *
 * <p>Devuelto por {@code GET /api/billing/status} para que el panel admin
 * muestre la información de plan y facturación del tenant.</p>
 */
@Data
@Builder
public class BillingStatusDTO {

    /** Nombre del plan activo: {@code STARTER}, {@code PRO}, {@code CLINIC}, {@code FOUNDER}. */
    private String plan;

    /** Etiqueta legible del plan (ej. "Plan Pro"). */
    private String planLabel;

    /** Estado de facturación: {@code ACTIVE}, {@code PENDING}, {@code SUSPENDED}, {@code EXEMPT}, {@code CANCELLED}. */
    private String billingStatus;

    /** Etiqueta legible del estado de facturación. */
    private String billingStatusLabel;

    /** Tipo de cuenta: {@code REGULAR} o {@code FOUNDER}. */
    private String accountType;

    /** Precio mensual del plan actual en COP. */
    private long monthlyPriceCOP;

    /** Precio anual del plan actual en COP (con 20% de descuento). */
    private long annualPriceCOP;

    /** Nombre de la clínica (tenant). */
    private String clinicName;

    /** Email del propietario del tenant. */
    private String ownerEmail;
}
