package com.demo.ecosalud.enums;

/**
 * Estado de facturación de la suscripción de un tenant.
 */
public enum BillingStatus {
    /** No genera facturas (fundadores, demos). */
    EXEMPT,
    /** Suscripción activa y al día. */
    ACTIVE,
    /** Pago pendiente — acceso reducido. */
    PENDING,
    /** Suscripción suspendida por falta de pago. */
    SUSPENDED,
    /** Cuenta cancelada definitivamente. */
    CANCELLED
}
