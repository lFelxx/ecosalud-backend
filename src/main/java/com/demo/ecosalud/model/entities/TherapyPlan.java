package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para planes de terapia multi-sesión.
 * Mapea a la tabla {@code therapy_plans} en el schema del tenant activo.
 *
 * <p>Un plan de terapia agrupa un número definido de sesiones para un paciente
 * con un servicio específico. El campo {@code status} sigue la convención
 * española del backend: ACTIVO | PAUSADO | COMPLETADO | CANCELADO.</p>
 */
@Entity
@Data
@Table(name = "therapy_plans")
public class TherapyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** FK al paciente en {@code tenant.users}. */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** FK al especialista en {@code tenant.specialists} (opcional). */
    @Column(name = "specialist_id")
    private Long specialistId;

    /** FK al servicio en {@code tenant.services} (opcional). */
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "total_sessions", nullable = false)
    private int totalSessions;

    @Column(name = "completed_sessions", nullable = false)
    private int completedSessions = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Estado del plan: ACTIVO | PAUSADO | COMPLETADO | CANCELADO.
     * Mapeo frontend: ACTIVO→active, PAUSADO→paused, etc.
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = "ACTIVO";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
