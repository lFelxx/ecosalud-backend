package com.demo.ecosalud.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estadísticas de uso de una clínica, consultadas por el super-admin
 * para dar soporte y monitorear la actividad de cada tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatsDTO {

    /** Usuarios registrados en el schema del tenant (pacientes + admins). */
    private long totalUsers;

    /** Citas agendadas en total. */
    private long totalAppointments;

    /** Citas pendientes o en curso. */
    private long pendingAppointments;

    /** Especialistas activos. */
    private long totalSpecialists;

    /** Servicios activos ofrecidos. */
    private long totalServices;

    /** Planes de terapia activos. */
    private long totalTherapyPlans;

    /** Historias clínicas creadas. */
    private long totalHealthRecords;

    /** Publicaciones del blog. */
    private long totalPosts;
}
