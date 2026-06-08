package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Consentimiento informado digital firmado por el paciente.
 *
 * <p>Mapea a la tabla {@code consents} en el schema del tenant activo.</p>
 *
 * <p>Una vez firmado, el registro es <b>inmutable</b>: representa evidencia
 * legal del consentimiento del paciente para un servicio médico.
 * El texto del formulario se almacena en el momento de la firma
 * ({@code consent_text}) para preservar exactamente lo que fue aceptado.</p>
 */
@Entity
@Data
@Table(name = "consents")
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK al paciente que firmó el consentimiento. */
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    /** FK al servicio / terapia asociada. Puede ser null para consentimientos generales. */
    @Column(name = "service_id")
    private Long serviceId;

    /**
     * Texto completo del formulario de consentimiento en el momento de la firma.
     * Almacenado textualmente para garantizar inmutabilidad legal.
     */
    @Column(name = "consent_text", columnDefinition = "TEXT", nullable = false)
    private String consentText;

    /**
     * Firma digital: nombre completo escrito por el paciente al confirmar.
     * Equivalente funcional a "firma manuscrita digitalizada" para uso en plataforma SaaS.
     */
    @Column(name = "digital_signature", columnDefinition = "TEXT")
    private String digitalSignature;

    /** Dirección IP del cliente en el momento de la firma. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Fecha y hora exacta de la firma. */
    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
