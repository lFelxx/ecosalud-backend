package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la historia clínica electrónica por tenant.
 *
 * <p>Mapea a la tabla {@code health_records} en el schema del tenant activo.
 * Diseñada para cumplir la <strong>Resolución 1995/1999 de MinSalud</strong>:</p>
 * <ul>
 *   <li>Motivo de consulta, anamnesis, examen físico, diagnóstico y
 *       plan de tratamiento son campos clínicos mínimos requeridos.</li>
 *   <li>El campo {@code locked} bloquea la historia para impedir
 *       modificaciones posteriores (inmutabilidad exigida por la resolución).</li>
 *   <li>{@code hashIntegrity} almacena un SHA-256 de los campos clínicos
 *       para detectar manipulación del registro.</li>
 * </ul>
 *
 * <p>TODO Fase 3 — al integrar con SISPRO (Res. 2654/2019) agregar
 * campos HL7 FHIR y referencia al identificador único nacional del paciente.</p>
 */
@Entity
@Data
@Table(name = "health_records")
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // ── Vínculos (almacenados como IDs simples para evitar dependencias ────────
    // ── cruzadas con las entidades Appointment y Specialist que aún           ──
    // ── no tienen relaciones JPA completas definidas en este schema)          ──

    /** ID del paciente en la tabla {@code tenant.users}. */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** ID de la cita vinculada (opcional). */
    @Column(name = "appointment_id")
    private Long appointmentId;

    /** ID del especialista que elaboró la historia (opcional). */
    @Column(name = "specialist_id")
    private Long specialistId;

    // ── Campos clínicos mínimos — Res. 1995/1999 MinSalud ────────────────────

    /** Motivo de consulta — OBLIGATORIO según Res. 1995/1999. */
    @Column(name = "reason_for_consultation", columnDefinition = "TEXT")
    private String reasonForConsultation;

    /** Enfermedad actual / anamnesis (historia de la enfermedad). */
    @Column(name = "current_illness", columnDefinition = "TEXT")
    private String currentIllness;

    /** Hallazgos del examen físico. */
    @Column(name = "physical_exam", columnDefinition = "TEXT")
    private String physicalExam;

    /** Diagnóstico clínico — OBLIGATORIO según Res. 1995/1999. */
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    /** Plan de tratamiento prescrito. */
    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    /** Observaciones adicionales del profesional. */
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    /** Fecha sugerida para la próxima consulta. */
    @Column(name = "next_appointment")
    private LocalDate nextAppointment;

    // ── Integridad y trazabilidad ─────────────────────────────────────────────

    /**
     * Indica si la historia fue bloqueada por el profesional.
     * Una historia <em>bloqueada</em> no puede ser editada ni eliminada.
     * Valor inicial: {@code false}.
     */
    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    /**
     * Hash SHA-256 de los campos clínicos para detectar manipulación.
     * Se regenera en cada escritura (create / update).
     */
    @Column(name = "hash_integrity", length = 64)
    private String hashIntegrity;

    /** ID del usuario del sistema que creó la historia. */
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
