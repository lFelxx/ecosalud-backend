package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.fhir.FhirBundle;
import com.demo.ecosalud.model.dto.fhir.FhirCapabilityStatement;
import com.demo.ecosalud.model.dto.fhir.FhirEncounter;
import com.demo.ecosalud.model.dto.fhir.FhirPatient;

import java.util.Optional;

/**
 * Servicio de mapeo y consulta de recursos HL7 FHIR R4.
 *
 * <p>Transforma las entidades del dominio ({@code User}, {@code Appointment})
 * a recursos FHIR R4 ({@code Patient}, {@code Encounter}) y produce
 * Bundles de búsqueda para las operaciones {@code search-type}.</p>
 *
 * <p>Todos los métodos operan <strong>dentro del tenant activo</strong>
 * definido por {@code TenantContext} — el contexto de tenant se resuelve
 * desde el JWT del request HTTP (igual que el resto del sistema).</p>
 *
 * <p>Normativa aplicable:</p>
 * <ul>
 *   <li>HL7 FHIR R4 — <a href="https://hl7.org/fhir/R4/">https://hl7.org/fhir/R4/</a></li>
 *   <li>Resolución 2654/2019 MinSalud Colombia — Interoperabilidad HCE</li>
 *   <li>SISPRO — Sistema Integral de Información de la Protección Social</li>
 * </ul>
 */
public interface FhirMapperService {

    // ── CapabilityStatement ───────────────────────────────────────────────────

    /**
     * Genera el CapabilityStatement de este servidor FHIR.
     *
     * <p>Describe qué recursos y operaciones están disponibles.
     * Se expone en {@code GET /api/fhir/metadata} (público, sin JWT).</p>
     *
     * @return CapabilityStatement en FHIR R4
     */
    FhirCapabilityStatement getCapabilityStatement();

    // ── Patient ───────────────────────────────────────────────────────────────

    /**
     * Retorna todos los pacientes del tenant activo como un Bundle FHIR.
     *
     * <p>Solo se incluyen usuarios con {@code role = USER}.</p>
     *
     * @return Bundle de tipo {@code searchset} con todos los recursos Patient
     */
    FhirBundle searchPatients();

    /**
     * Retorna un paciente específico por su ID en el tenant activo.
     *
     * @param id identificador del usuario/paciente
     * @return Optional con el recurso Patient, o vacío si no existe o no es paciente
     */
    Optional<FhirPatient> getPatientById(Long id);

    // ── Encounter ─────────────────────────────────────────────────────────────

    /**
     * Retorna los encuentros clínicos del tenant activo como un Bundle FHIR.
     *
     * <p>Parámetros opcionales de filtro:</p>
     * <ul>
     *   <li>{@code patientId} — filtra por paciente (equivale a {@code ?patient=Patient/123})</li>
     *   <li>{@code status} — filtra por estado FHIR (planned | arrived | finished | cancelled)</li>
     * </ul>
     *
     * @param patientId ID del paciente para filtrar (null = todos)
     * @param statusFhir estado FHIR para filtrar (null = todos)
     * @return Bundle de tipo {@code searchset} con los recursos Encounter
     */
    FhirBundle searchEncounters(Long patientId, String statusFhir);

    /**
     * Retorna un encuentro clínico específico por su ID en el tenant activo.
     *
     * @param id identificador de la cita/encuentro
     * @return Optional con el recurso Encounter, o vacío si no existe
     */
    Optional<FhirEncounter> getEncounterById(Long id);
}
