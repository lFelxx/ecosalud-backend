package com.demo.ecosalud.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para planes de terapia multi-sesión.
 *
 * <p>{@code patientName}, {@code patientEmail} y {@code serviceName} son
 * campos de solo salida resueltos por el servicio a partir de los IDs.</p>
 */
@Data
public class TherapyPlanDTO {
    private Long id;

    // Paciente
    private Long patientId;
    private String patientName;
    private String patientEmail;

    // Servicio
    private Long serviceId;
    private String serviceName;

    private Long specialistId;

    private int totalSessions;
    private int completedSessions;
    private LocalDate startDate;

    /**
     * Estado: ACTIVO | PAUSADO | COMPLETADO | CANCELADO.
     * Mapeo frontend: ACTIVO→active, PAUSADO→paused, etc.
     */
    private String status;
    private String notes;

    private LocalDateTime createdAt;
}
