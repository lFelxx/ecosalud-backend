package com.demo.ecosalud.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO para citas médicas.
 *
 * <p>Los campos {@code patientName}, {@code patientEmail} y {@code serviceName}
 * son solo de salida — el backend los resuelve a partir de los IDs.</p>
 */
@Data
public class AppointmentDTO {
    private Long id;

    // Paciente
    private Long patientId;
    private String patientName;
    private String patientEmail;

    // Servicio / Especialista
    private Long serviceId;
    private String serviceName;
    private Long specialistId;

    // Fecha y hora
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    /**
     * Estado: PENDIENTE | CONFIRMADA | COMPLETADA | CANCELADA.
     * El frontend mapea a: pending | confirmed | completed | cancelled.
     */
    private String status;
    private String notes;
    private String cancellationReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
