package com.demo.ecosalud.model.dto.fhir;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Recurso <strong>FHIR R4 Patient</strong>.
 *
 * <p>Referencia: <a href="https://hl7.org/fhir/R4/patient.html">
 * https://hl7.org/fhir/R4/patient.html</a></p>
 *
 * <p>Mapeado desde {@code tenant.users} donde {@code role = USER}.
 * Campos disponibles en nuestra entidad {@link com.demo.ecosalud.model.entities.User}:</p>
 * <ul>
 *   <li>{@code id} → Patient.id</li>
 *   <li>{@code name} → Patient.name[0].text (nombre completo)</li>
 *   <li>{@code email} → Patient.telecom[system=email].value</li>
 *   <li>{@code status} → Patient.active (ACTIVE → true)</li>
 *   <li>{@code createdAt} → Patient.meta.lastUpdated</li>
 * </ul>
 *
 * <p><em>Nota SISPRO:</em> Para cumplimiento completo con la Res. 2654/2019
 * se requieren campos adicionales (tipo y número de documento, fecha de nacimiento,
 * género, municipio de residencia). Estos deberán agregarse al modelo {@code User}
 * en Fase 4.</p>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirPatient {

    /**
     * Tipo del recurso — siempre {@code "Patient"}.
     * Obligatorio en la representación JSON según la spec FHIR.
     */
    @JsonProperty("resourceType")
    @Builder.Default
    private String resourceType = "Patient";

    /** Identificador lógico del recurso en este servidor. */
    private String id;

    /** Metadatos del recurso (versión, fecha de última modificación, perfil). */
    private FhirTypes.Meta meta;

    /**
     * Identificadores del paciente.
     *
     * <p>Incluye el ID interno del sistema. Para SISPRO debe incluirse
     * también el número de documento (CC, CE, PA, etc.) con su tipo según
     * la tabla {@code 2.16.840.1.113883.2.23.1} (Colombia).</p>
     */
    private List<FhirTypes.Identifier> identifier;

    /**
     * Indica si el registro del paciente está activo.
     * {@code true} si {@code User.status = ACTIVE}, {@code false} si INACTIVE.
     */
    private Boolean active;

    /**
     * Nombre(s) del paciente.
     * El primero de la lista con {@code use = "official"} es el nombre principal.
     */
    private List<FhirTypes.HumanName> name;

    /**
     * Datos de contacto: teléfono, email.
     * El email se mapea con {@code system = "email"}.
     */
    private List<FhirTypes.ContactPoint> telecom;

    /**
     * Sexo administrativo: {@code male | female | other | unknown}.
     * En la versión actual siempre es {@code "unknown"} pues el campo
     * no existe en la entidad User. Será expandido en Fase 4.
     */
    private String gender;
}
