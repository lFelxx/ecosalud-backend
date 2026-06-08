package com.demo.ecosalud.enums;

/**
 * Planes de suscripción disponibles en la plataforma Ecosalud Market.
 *
 * <p>Cada constante expone sus límites operacionales a través de los métodos
 * {@link #maxAppointmentsPerMonth()} y {@link #maxPatients()}, usados por
 * {@code PlanLimitsService} para hacer cumplir las restricciones antes de
 * persistir nuevas citas o pacientes.</p>
 *
 * <table>
 *   <tr><th>Plan</th><th>Citas/mes</th><th>Pacientes</th></tr>
 *   <tr><td>STARTER</td><td>50</td><td>30</td></tr>
 *   <tr><td>PRO</td><td>300</td><td>200</td></tr>
 *   <tr><td>CLINIC</td><td>Ilimitado</td><td>Ilimitado</td></tr>
 *   <tr><td>FOUNDER</td><td>Ilimitado</td><td>Ilimitado</td></tr>
 * </table>
 */
public enum SubscriptionPlan {

    /**
     * Plan de entrada — 1 especialista, 50 citas/mes, 30 pacientes.
     * Subdominio automático incluido.
     */
    STARTER,

    /**
     * Plan intermedio — hasta 3 especialistas, 300 citas/mes, 200 pacientes.
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
    FOUNDER;

    // ── Límites operacionales ────────────────────────────────────────────────

    /**
     * Máximo de citas (por fecha de cita) permitidas en un mes calendario.
     * {@link Integer#MAX_VALUE} representa ilimitado.
     */
    public int maxAppointmentsPerMonth() {
        return switch (this) {
            case STARTER        -> 50;
            case PRO            -> 300;
            case CLINIC, FOUNDER -> Integer.MAX_VALUE;
        };
    }

    /**
     * Máximo de pacientes (usuarios con rol USER) registrados en el tenant.
     * {@link Integer#MAX_VALUE} representa ilimitado.
     */
    public int maxPatients() {
        return switch (this) {
            case STARTER        -> 30;
            case PRO            -> 200;
            case CLINIC, FOUNDER -> Integer.MAX_VALUE;
        };
    }

    /** {@code true} si el plan no tiene ningún límite operacional. */
    public boolean isUnlimited() {
        return this == CLINIC || this == FOUNDER;
    }
}
