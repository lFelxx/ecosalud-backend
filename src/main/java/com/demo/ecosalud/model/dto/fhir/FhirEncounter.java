package com.demo.ecosalud.model.dto.fhir;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Recurso <strong>FHIR R4 Encounter</strong>.
 *
 * <p>Referencia: <a href="https://hl7.org/fhir/R4/encounter.html">
 * https://hl7.org/fhir/R4/encounter.html</a></p>
 *
 * <p>Mapeado desde {@code tenant.appointments}:</p>
 * <ul>
 *   <li>{@code id} → Encounter.id</li>
 *   <li>{@code status} → Encounter.status (ver mapeo abajo)</li>
 *   <li>{@code patientId} → Encounter.subject.reference = "Patient/{patientId}"</li>
 *   <li>{@code appointmentDate + appointmentTime} → Encounter.period.start</li>
 *   <li>{@code notes} → Encounter.reasonCode[0].text</li>
 * </ul>
 *
 * <h3>Mapeo de estados</h3>
 * <table border="1">
 *   <tr><th>Sistema Ecosalud</th><th>FHIR R4</th></tr>
 *   <tr><td>PENDIENTE</td><td>planned</td></tr>
 *   <tr><td>CONFIRMADA</td><td>arrived</td></tr>
 *   <tr><td>COMPLETADA</td><td>finished</td></tr>
 *   <tr><td>CANCELADA</td><td>cancelled</td></tr>
 *   <tr><td>(otro)</td><td>unknown</td></tr>
 * </table>
 *
 * <p><em>Nota:</em> {@code class} es palabra reservada en Java; se usa
 * {@code @JsonProperty("class")} para la serialización correcta.</p>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirEncounter {

    /**
     * Tipo del recurso — siempre {@code "Encounter"}.
     * Obligatorio en la representación JSON según la spec FHIR.
     */
    @JsonProperty("resourceType")
    @Builder.Default
    private String resourceType = "Encounter";

    /** Identificador lógico del recurso en este servidor. */
    private String id;

    /** Metadatos del recurso. */
    private FhirTypes.Meta meta;

    /**
     * Estado actual del encuentro clínico.
     * Valores FHIR R4: {@code planned | arrived | triaged | in-progress |
     * onleave | finished | cancelled | entered-in-error | unknown}
     */
    private String status;

    /**
     * Clasificación del encuentro (sistema ActCode de HL7).
     * Para Ecosalud siempre {@code AMB} (ambulatorio).
     * Serializado como {@code "class"} en JSON (palabra reservada en Java).
     */
    @JsonProperty("class")
    private FhirTypes.Coding encounterClass;

    /** Paciente al que pertenece el encuentro. */
    private FhirTypes.Reference subject;

    /** Intervalo de tiempo del encuentro (fecha/hora de inicio). */
    private FhirTypes.Period period;

    /**
     * Razón codificada de la consulta.
     * Se usa para almacenar las notas/motivo de la cita como texto libre
     * cuando no hay código SNOMED disponible.
     */
    private List<FhirTypes.CodeableConcept> reasonCode;
}
