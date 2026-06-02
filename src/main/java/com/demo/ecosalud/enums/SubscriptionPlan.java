package com.demo.ecosalud.enums;

/**
 * Planes de suscripción disponibles en la plataforma Ecosalud Market.
 *
 * <p>Cada plan habilita un conjunto diferente de funcionalidades
 * gestionado por {@code FeatureFlagResolver}.</p>
 */
public enum SubscriptionPlan {

    /**
     * Plan de entrada — 1 especialista, 50 citas/mes, 5 servicios.
     * Subdominio automático incluido.
     */
    STARTER,

    /**
     * Plan intermedio — hasta 3 especialistas, 300 citas/mes, 20 servicios.
     * Incluye historial clínico básico y guía para dominio propio.
     */
    PRO,

    /**
     * Plan completo — especialistas, citas y servicios ilimitados.
     * Incluye multi-sede, dominio propio integrado y soporte prioritario.
     */
    CLINIC,

    /**
     * Plan especial para clínicas fundadoras de la plataforma.
     * Equivalente a CLINIC con acceso ilimitado y sin cobro.
     */
    FOUNDER
}
