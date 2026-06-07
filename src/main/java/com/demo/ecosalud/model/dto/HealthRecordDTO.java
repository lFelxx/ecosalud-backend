package com.demo.ecosalud.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de historia clínica electrónica (Res. 1995/1999 MinSalud).
 *
 * <p>Se usa tanto para entrada (crear / actualizar) como para salida (respuesta de la API).
 * Los campos {@code patientName} y los timestamps son solo de salida (el backend los rellena).</p>
 */
@Data
public class HealthRecordDTO {

    private Long id;

    // ── Paciente ──────────────────────────────────────────────────────────────

    /** ID del paciente en {@code tenant.users} — obligatorio al crear. */
    private Long patientId;

    /**
     * Nombre del paciente — solo salida, resuelto por el servicio
     * a partir del {@code patientId}.
     */
    private String patientName;

    // ── Vínculos opcionales ───────────────────────────────────────────────────

    /** ID de la cita clínica vinculada (puede ser {@code null}). */
    private Long appointmentId;

    /** ID del especialista que registra la historia (puede ser {@code null}). */
    private Long specialistId;

    // ── Campos clínicos mínimos — Res. 1995/1999 ─────────────────────────────

    /** Motivo de consulta — OBLIGATORIO. */
    private String reasonForConsultation;

    /** Enfermedad actual / anamnesis. */
    private String currentIllness;

    /** Examen físico. */
    private String physicalExam;

    /** Diagnóstico clínico — OBLIGATORIO. */
    private String diagnosis;

    /** Plan de tratamiento. */
    private String treatmentPlan;

    /** Observaciones adicionales. */
    private String observations;

    /** Próxima cita sugerida. */
    private LocalDate nextAppointment;

    // ── Trazabilidad — solo salida ────────────────────────────────────────────

    /** Indica si la historia está bloqueada (inmutable). */
    private boolean locked;

    /** Hash SHA-256 de los campos clínicos para verificar integridad. */
    private String hashIntegrity;

    /** ID del usuario del sistema que creó la historia. */
    private Long createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
