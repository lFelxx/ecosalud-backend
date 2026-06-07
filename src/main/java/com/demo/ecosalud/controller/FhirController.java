package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.fhir.*;
import com.demo.ecosalud.service.FhirMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST de la interfaz <strong>HL7 FHIR R4</strong> de Ecosalud.
 *
 * <h3>Ruta base: {@code /api/fhir}</h3>
 *
 * <p>Implementa el subconjunto de operaciones FHIR R4 requeridas por
 * la <strong>Resolución 2654/2019 del Ministerio de Salud de Colombia</strong>
 * para la interoperabilidad de Historias Clínicas Electrónicas con SISPRO.</p>
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET /api/fhir/metadata             → CapabilityStatement (público — sin JWT)
 * GET /api/fhir/Patient              → Bundle searchset de pacientes (JWT requerido)
 * GET /api/fhir/Patient/{id}         → Recurso Patient individual (JWT requerido)
 * GET /api/fhir/Encounter            → Bundle searchset de encuentros (JWT requerido)
 * GET /api/fhir/Encounter/{id}       → Recurso Encounter individual (JWT requerido)
 * </pre>
 *
 * <h3>Parámetros de búsqueda soportados</h3>
 * <ul>
 *   <li>{@code GET /api/fhir/Encounter?patient=5} — encuentros de un paciente</li>
 *   <li>{@code GET /api/fhir/Encounter?status=finished} — por estado FHIR</li>
 * </ul>
 *
 * <h3>Content-Type</h3>
 * <p>Todas las respuestas se devuelven como {@code application/fhir+json}
 * (con charset UTF-8). Se acepta también {@code application/json}.</p>
 *
 * <h3>Errores</h3>
 * <p>Los errores se devuelven como {@code OperationOutcome} con el código
 * HTTP correspondiente (404 Not Found, 422 Unprocessable Entity, etc.).</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/fhir")
@RequiredArgsConstructor
public class FhirController {

    /** Content-Type estándar para FHIR R4 JSON. */
    private static final String FHIR_CONTENT_TYPE = "application/fhir+json;charset=UTF-8";

    private final FhirMapperService fhirMapper;

    // ── CapabilityStatement ───────────────────────────────────────────────────

    /**
     * Devuelve el CapabilityStatement del servidor FHIR.
     *
     * <p>Endpoint <strong>público</strong> — no requiere autenticación JWT.
     * Equivalente a {@code GET [base]/metadata} en la spec FHIR.</p>
     *
     * @return 200 OK con el CapabilityStatement en {@code application/fhir+json}
     */
    @GetMapping(value = "/metadata",
                produces = {FHIR_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FhirCapabilityStatement> getMetadata() {
        log.debug("[FHIR] GET /metadata");
        return ResponseEntity.ok()
            .header("Content-Type", FHIR_CONTENT_TYPE)
            .body(fhirMapper.getCapabilityStatement());
    }

    // ── Patient ───────────────────────────────────────────────────────────────

    /**
     * Búsqueda de recursos Patient (pacientes del tenant activo).
     *
     * <p>Retorna todos los usuarios con {@code role = USER} como recursos FHIR Patient.</p>
     *
     * @return 200 OK con Bundle de tipo {@code searchset}
     */
    @GetMapping(value = "/Patient",
                produces = {FHIR_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FhirBundle> searchPatients() {
        log.debug("[FHIR] GET /Patient");
        FhirBundle bundle = fhirMapper.searchPatients();
        return ResponseEntity.ok()
            .header("Content-Type", FHIR_CONTENT_TYPE)
            .body(bundle);
    }

    /**
     * Lectura de un recurso Patient específico por su ID.
     *
     * @param id ID numérico del paciente en el sistema Ecosalud
     * @return 200 OK con el recurso Patient, o 404 con OperationOutcome si no existe
     */
    @GetMapping(value = "/Patient/{id}",
                produces = {FHIR_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getPatient(@PathVariable Long id) {
        log.debug("[FHIR] GET /Patient/{}", id);

        Optional<FhirPatient> patient = fhirMapper.getPatientById(id);

        if (patient.isEmpty()) {
            return notFound("Patient/" + id);
        }

        return ResponseEntity.ok()
            .header("Content-Type", FHIR_CONTENT_TYPE)
            .body(patient.get());
    }

    // ── Encounter ─────────────────────────────────────────────────────────────

    /**
     * Búsqueda de recursos Encounter (citas/encuentros del tenant activo).
     *
     * <p>Parámetros de búsqueda opcionales:</p>
     * <ul>
     *   <li>{@code ?patient=5} — filtra por ID de paciente</li>
     *   <li>{@code ?status=finished} — filtra por estado FHIR</li>
     * </ul>
     *
     * @param patient  ID del paciente para filtrar (opcional)
     * @param status   estado FHIR para filtrar: planned|arrived|finished|cancelled (opcional)
     * @return 200 OK con Bundle de tipo {@code searchset}
     */
    @GetMapping(value = "/Encounter",
                produces = {FHIR_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<FhirBundle> searchEncounters(
            @RequestParam(required = false) Long patient,
            @RequestParam(required = false) String status) {

        log.debug("[FHIR] GET /Encounter?patient={}&status={}", patient, status);
        FhirBundle bundle = fhirMapper.searchEncounters(patient, status);
        return ResponseEntity.ok()
            .header("Content-Type", FHIR_CONTENT_TYPE)
            .body(bundle);
    }

    /**
     * Lectura de un recurso Encounter específico por su ID.
     *
     * @param id ID numérico de la cita en el sistema Ecosalud
     * @return 200 OK con el recurso Encounter, o 404 con OperationOutcome si no existe
     */
    @GetMapping(value = "/Encounter/{id}",
                produces = {FHIR_CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getEncounter(@PathVariable Long id) {
        log.debug("[FHIR] GET /Encounter/{}", id);

        Optional<FhirEncounter> encounter = fhirMapper.getEncounterById(id);

        if (encounter.isEmpty()) {
            return notFound("Encounter/" + id);
        }

        return ResponseEntity.ok()
            .header("Content-Type", FHIR_CONTENT_TYPE)
            .body(encounter.get());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Construye una respuesta 404 en formato FHIR {@code OperationOutcome}.
     *
     * @param resourceRef referencia legible al recurso no encontrado (p.ej. "Patient/99")
     */
    private ResponseEntity<Object> notFound(String resourceRef) {
        FhirOperationOutcome outcome = FhirOperationOutcome.builder()
            .issue(List.of(
                FhirOperationOutcome.Issue.builder()
                    .severity("error")
                    .code("not-found")
                    .diagnostics(resourceRef + " no encontrado en este tenant.")
                    .build()
            ))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .header("Content-Type", FHIR_CONTENT_TYPE)
            .body(outcome);
    }
}
