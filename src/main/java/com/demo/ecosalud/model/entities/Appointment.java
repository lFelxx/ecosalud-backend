package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad JPA para las citas médicas del tenant.
 * Mapea a la tabla {@code appointments} en el schema del tenant activo.
 *
 * <p>Los FKs (patient_id, specialist_id, service_id) se almacenan como
 * Long simples para compatibilidad con el schema multi-tenant;
 * el nombre del paciente y el servicio se resuelven en la capa de servicio.</p>
 */
@Entity
@Data
@Table(name = "appointments")
public class Appointment {

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

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    /**
     * Estado de la cita: PENDIENTE | CONFIRMADA | COMPLETADA | CANCELADA.
     * Se almacena como String para no depender del enum {@code AppointmentSatus}
     * y alinearse directamente con los valores del schema.
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDIENTE";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
