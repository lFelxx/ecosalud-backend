package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO de respuesta del dashboard de ingresos del super-admin.
 *
 * <p>Contiene todas las métricas financieras y de crecimiento de la plataforma
 * calculadas en tiempo real desde {@code public.tenants}.</p>
 */
@Data
@Builder
public class RevenueDTO {

    // ── MRR / ARR ────────────────────────────────────────────────────────────

    /** Ingresos recurrentes mensuales (COP) — solo tenants con billingStatus=ACTIVE. */
    private long mrrCOP;

    /** Ingresos recurrentes anuales (COP) = MRR × 12. */
    private long arrCOP;

    /** MRR potencial (COP) si todos los tenants en TRIAL convirtieran al pago. */
    private long potentialMrrCOP;

    // ── Conteos por estado ───────────────────────────────────────────────────

    /** Tenants con suscripción activa y pagando. */
    private int activeTenants;

    /** Tenants en período de prueba (14 días). */
    private int trialTenants;

    /** Tenants con trial o pago vencido. */
    private int overdueTenants;

    /** Tenants fundadores exentos de pago. */
    private int exemptTenants;

    /** Total de tenants activos en la plataforma ({@code active=true}). */
    private int totalTenants;

    // ── Distribución por plan (tenants ACTIVE) ───────────────────────────────

    /** Tenants activos en plan STARTER. */
    private int starterActive;

    /** Tenants activos en plan PRO. */
    private int proActive;

    /** Tenants activos en plan CLINIC. */
    private int clinicActive;

    /** Tenants fundadores (plan FOUNDER, siempre EXEMPT). */
    private int founderCount;

    // ── Métricas de conversión ───────────────────────────────────────────────

    /**
     * Tasa de conversión (%) = activeTenants / (activeTenants + overdueTenants) × 100.
     * Mide qué porcentaje de los tenants que terminaron el trial terminaron pagando.
     * 0.0 si no hay datos suficientes.
     */
    private double conversionRate;

    // ── Crecimiento mensual (últimos 6 meses) ────────────────────────────────

    /** Nuevas clínicas registradas y MRR generado, mes a mes (últimos 6 meses). */
    private List<MonthlyGrowth> last6Months;

    // ── Sub-DTO ──────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class MonthlyGrowth {
        /** Nombre del mes y año. Ejemplo: {@code "Jun 2026"}. */
        private String month;

        /** Nuevas clínicas registradas en ese mes. */
        private int newClinics;

        /**
         * MRR acumulado (COP) generado por los tenants creados ese mes
         * que actualmente están en estado ACTIVE.
         */
        private long mrrCOP;
    }
}
