package com.demo.ecosalud.enums;

/**
 * Estado de facturación de la suscripción de un tenant.
 *
 * <p>Ciclo de vida habitual de una clínica nueva:</p>
 * <pre>
 *   TRIAL (14 días) → ACTIVE (pago exitoso) o OVERDUE (trial vencido sin pago)
 *   OVERDUE → ACTIVE (paga) | SUSPENDED (60+ días sin pago) | CANCELLED
 * </pre>
 */
public enum BillingStatus {
    /** No genera facturas — fundadores y demos con acceso ilimitado sin cobro. */
    EXEMPT,
    /** Período de prueba gratuito (14 días desde el registro). Acceso completo. */
    TRIAL,
    /** Suscripción activa y al día. */
    ACTIVE,
    /** Trial expirado o pago vencido — creación de nuevos recursos bloqueada. */
    OVERDUE,
    /** Pago en proceso de verificación (webhook PayU recibido). */
    PENDING,
    /** Suscripción suspendida por falta de pago prolongado. */
    SUSPENDED,
    /** Cuenta cancelada definitivamente. */
    CANCELLED
}
