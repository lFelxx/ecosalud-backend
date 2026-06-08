package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Resumen estadístico de la clínica para el módulo de reportes del admin.
 *
 * <p>Generado por {@code ReportsController} con datos en tiempo real
 * del tenant activo.</p>
 */
@Data
@Builder
public class ReportsSummaryDTO {

    // ── Citas ─────────────────────────────────────────────────────────────────

    /** Total de citas registradas en el sistema. */
    private long totalAppointments;

    /** Citas pendientes de confirmación. */
    private long pendingAppointments;

    /** Citas confirmadas (próximas). */
    private long confirmedAppointments;

    /** Citas completadas (historial). */
    private long completedAppointments;

    /** Citas canceladas. */
    private long cancelledAppointments;

    // ── Pacientes ─────────────────────────────────────────────────────────────

    /** Total de pacientes (usuarios con rol USER/PATIENT). */
    private long totalPatients;

    // ── Servicios ─────────────────────────────────────────────────────────────

    /** Total de servicios activos. */
    private long totalServices;

    /**
     * Top 5 servicios más solicitados: nombre → número de citas.
     * Lista de pares [nombre, cantidad] ordenados descendentemente.
     */
    private List<ServiceStatDTO> topServices;

    // ── Planes de terapia ─────────────────────────────────────────────────────

    /** Total de planes de terapia. */
    private long totalTherapyPlans;

    /** Planes activos actualmente. */
    private long activeTherapyPlans;

    // ── Historia clínica ──────────────────────────────────────────────────────

    /** Total de registros en la historia clínica electrónica. */
    private long totalHealthRecords;

    // ── Resumen por estado ────────────────────────────────────────────────────

    /**
     * Porcentaje de ocupación de citas (completadas / total).
     * Valor entre 0 y 100.
     */
    private int occupancyRate;

    // ── Nested DTO ────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class ServiceStatDTO {
        private String serviceName;
        private long   count;
        private int    percentage; // % respecto al total de citas
    }
}
