package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.model.dto.fhir.*;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.FhirMapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de {@link FhirMapperService}.
 *
 * <h3>Mapeo de entidades a recursos FHIR R4</h3>
 *
 * <p><strong>Patient ← User (role=USER)</strong></p>
 * <table border="1">
 *   <tr><th>User</th><th>FHIR Patient</th></tr>
 *   <tr><td>id</td><td>id, identifier[0].value</td></tr>
 *   <tr><td>name</td><td>name[0].text</td></tr>
 *   <tr><td>email</td><td>telecom[email].value</td></tr>
 *   <tr><td>status=ACTIVE</td><td>active=true</td></tr>
 *   <tr><td>createdAt</td><td>meta.lastUpdated</td></tr>
 * </table>
 *
 * <p><strong>Encounter ← Appointment</strong></p>
 * <table border="1">
 *   <tr><th>Appointment</th><th>FHIR Encounter</th></tr>
 *   <tr><td>id</td><td>id</td></tr>
 *   <tr><td>status (PENDIENTE→planned, etc.)</td><td>status</td></tr>
 *   <tr><td>patientId</td><td>subject.reference="Patient/{patientId}"</td></tr>
 *   <tr><td>appointmentDate + appointmentTime</td><td>period.start</td></tr>
 *   <tr><td>notes</td><td>reasonCode[0].text</td></tr>
 *   <tr><td>createdAt</td><td>meta.lastUpdated</td></tr>
 * </table>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FhirMapperServiceImpl implements FhirMapperService {

    private final UserRepository        userRepository;
    private final AppointmentRepository appointmentRepository;

    /** URL base del servidor FHIR. Configurable vía {@code app.fhir.base-url}. */
    @Value("${app.fhir.base-url:http://localhost:8080/api/fhir}")
    private String fhirBaseUrl;

    /** Versión de la plataforma (usada en CapabilityStatement). */
    @Value("${app.version:1.0.0}")
    private String appVersion;

    private static final DateTimeFormatter FHIR_DATETIME =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // ── CapabilityStatement ───────────────────────────────────────────────────

    @Override
    public FhirCapabilityStatement getCapabilityStatement() {
        return FhirCapabilityStatement.builder()
            .id("ecosalud-capability")
            .status("active")
            .date(LocalDateTime.now(ZoneOffset.UTC).format(FHIR_DATETIME))
            .publisher("Ecosalud Market S.A.S.")
            .description("""
                Servidor FHIR R4 de la plataforma Ecosalud Market.
                Implementa Patient y Encounter para interoperabilidad con SISPRO
                conforme a la Resolución 2654/2019 del Ministerio de Salud de Colombia.
                """)
            .kind("instance")
            .software(FhirCapabilityStatement.Software.builder()
                .name("Ecosalud Platform")
                .version(appVersion)
                .build())
            .fhirVersion("4.0.1")
            .format(List.of("json", "application/fhir+json"))
            .rest(List.of(buildRestCapability()))
            .build();
    }

    // ── Patient ───────────────────────────────────────────────────────────────

    @Override
    public FhirBundle searchPatients() {
        List<User> patients = userRepository.findByRole(RolUser.USER);

        List<FhirTypes.BundleEntry> entries = patients.stream()
            .map(this::toPatientEntry)
            .toList();

        log.debug("[FHIR] searchPatients → {} recursos Patient", entries.size());

        return FhirBundle.builder()
            .id(UUID.randomUUID().toString())
            .meta(FhirTypes.Meta.builder()
                .lastUpdated(nowFhir())
                .build())
            .type("searchset")
            .total(entries.size())
            .entry(entries.isEmpty() ? null : entries)
            .build();
    }

    @Override
    public Optional<FhirPatient> getPatientById(Long id) {
        return userRepository.findByIdAndRole(id, RolUser.USER)
            .map(this::toFhirPatient);
    }

    // ── Encounter ─────────────────────────────────────────────────────────────

    @Override
    public FhirBundle searchEncounters(Long patientId, String statusFhir) {
        List<Appointment> appointments;

        if (patientId != null) {
            appointments = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDesc(patientId);
        } else {
            appointments = appointmentRepository.findAll();
        }

        // Filtrar por estado FHIR si se especificó
        if (statusFhir != null && !statusFhir.isBlank()) {
            appointments = appointments.stream()
                .filter(a -> statusFhir.equalsIgnoreCase(mapStatusToFhir(a.getStatus())))
                .toList();
        }

        List<FhirTypes.BundleEntry> entries = appointments.stream()
            .map(this::toEncounterEntry)
            .toList();

        log.debug("[FHIR] searchEncounters(patient={}, status={}) → {} recursos Encounter",
            patientId, statusFhir, entries.size());

        return FhirBundle.builder()
            .id(UUID.randomUUID().toString())
            .meta(FhirTypes.Meta.builder()
                .lastUpdated(nowFhir())
                .build())
            .type("searchset")
            .total(entries.size())
            .entry(entries.isEmpty() ? null : entries)
            .build();
    }

    @Override
    public Optional<FhirEncounter> getEncounterById(Long id) {
        return appointmentRepository.findById(id)
            .map(this::toFhirEncounter);
    }

    // ── Mapeos internos ───────────────────────────────────────────────────────

    private FhirPatient toFhirPatient(User u) {
        return FhirPatient.builder()
            .id(String.valueOf(u.getId()))
            .meta(FhirTypes.Meta.builder()
                .versionId("1")
                .lastUpdated(u.getCreatedAt() != null
                    ? u.getCreatedAt().format(FHIR_DATETIME)
                    : nowFhir())
                .profile(List.of("http://hl7.org/fhir/StructureDefinition/Patient"))
                .build())
            .identifier(List.of(
                FhirTypes.Identifier.builder()
                    .use("official")
                    .system(fhirBaseUrl + "/Patient")
                    .value(String.valueOf(u.getId()))
                    .build()
            ))
            .active(u.getStatus() == UserStatus.ACTIVE)
            .name(List.of(
                FhirTypes.HumanName.builder()
                    .use("official")
                    .text(u.getName())
                    .build()
            ))
            .telecom(List.of(
                FhirTypes.ContactPoint.builder()
                    .system("email")
                    .value(u.getEmail())
                    .use("home")
                    .rank(1)
                    .build()
            ))
            .gender("unknown") // La entidad User no tiene campo gender en v1
            .build();
    }

    private FhirEncounter toFhirEncounter(Appointment a) {
        String periodStart = null;
        if (a.getAppointmentDate() != null && a.getAppointmentTime() != null) {
            periodStart = a.getAppointmentDate().atTime(a.getAppointmentTime())
                .format(FHIR_DATETIME);
        } else if (a.getAppointmentDate() != null) {
            periodStart = a.getAppointmentDate() + "T00:00:00Z";
        }

        FhirEncounter.FhirEncounterBuilder builder = FhirEncounter.builder()
            .id(String.valueOf(a.getId()))
            .meta(FhirTypes.Meta.builder()
                .versionId("1")
                .lastUpdated(a.getCreatedAt() != null
                    ? a.getCreatedAt().format(FHIR_DATETIME)
                    : nowFhir())
                .profile(List.of("http://hl7.org/fhir/StructureDefinition/Encounter"))
                .build())
            .status(mapStatusToFhir(a.getStatus()))
            .encounterClass(FhirTypes.Coding.builder()
                .system("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                .code("AMB")
                .display("ambulatory")
                .build())
            .subject(FhirTypes.Reference.builder()
                .reference("Patient/" + a.getPatientId())
                .build())
            .period(FhirTypes.Period.builder()
                .start(periodStart)
                .build());

        // Notas de la cita → reasonCode
        if (a.getNotes() != null && !a.getNotes().isBlank()) {
            builder.reasonCode(List.of(
                FhirTypes.CodeableConcept.builder()
                    .text(a.getNotes())
                    .build()
            ));
        }

        return builder.build();
    }

    private FhirTypes.BundleEntry toPatientEntry(User u) {
        return FhirTypes.BundleEntry.builder()
            .fullUrl(fhirBaseUrl + "/Patient/" + u.getId())
            .resource(toFhirPatient(u))
            .build();
    }

    private FhirTypes.BundleEntry toEncounterEntry(Appointment a) {
        return FhirTypes.BundleEntry.builder()
            .fullUrl(fhirBaseUrl + "/Encounter/" + a.getId())
            .resource(toFhirEncounter(a))
            .build();
    }

    // ── Mapeo de estados ──────────────────────────────────────────────────────

    /**
     * Convierte el estado interno de Ecosalud al código de estado FHIR R4.
     *
     * @param internalStatus estado en formato Ecosalud
     * @return código FHIR R4 del estado del Encounter
     */
    private String mapStatusToFhir(String internalStatus) {
        if (internalStatus == null) return "unknown";
        return switch (internalStatus.toUpperCase()) {
            case "PENDIENTE"  -> "planned";
            case "CONFIRMADA" -> "arrived";
            case "COMPLETADA" -> "finished";
            case "CANCELADA"  -> "cancelled";
            default           -> "unknown";
        };
    }

    // ── CapabilityStatement builder ───────────────────────────────────────────

    private FhirCapabilityStatement.Rest buildRestCapability() {
        return FhirCapabilityStatement.Rest.builder()
            .mode("server")
            .documentation("""
                Servidor FHIR R4 de solo lectura para recursos de salud del tenant.
                Requiere autenticación JWT (Bearer token) para Patient y Encounter.
                El endpoint /metadata es público.
                """)
            .security(FhirCapabilityStatement.Security.builder()
                .cors(true)
                .description("JWT Bearer token. Incluir en el header: Authorization: Bearer <token>")
                .service(List.of(
                    FhirTypes.CodeableConcept.builder()
                        .coding(List.of(
                            FhirTypes.Coding.builder()
                                .system("http://terminology.hl7.org/CodeSystem/restful-security-service")
                                .code("Bearer")
                                .display("Bearer Token")
                                .build()
                        ))
                        .text("JWT Bearer Token")
                        .build()
                ))
                .build())
            .resource(List.of(buildPatientCapability(), buildEncounterCapability()))
            .build();
    }

    private FhirCapabilityStatement.Resource buildPatientCapability() {
        return FhirCapabilityStatement.Resource.builder()
            .type("Patient")
            .profile("http://hl7.org/fhir/StructureDefinition/Patient")
            .interaction(List.of(
                FhirCapabilityStatement.Interaction.builder()
                    .code("read")
                    .documentation("GET /api/fhir/Patient/{id}")
                    .build(),
                FhirCapabilityStatement.Interaction.builder()
                    .code("search-type")
                    .documentation("GET /api/fhir/Patient")
                    .build()
            ))
            .searchParam(List.of(
                FhirCapabilityStatement.SearchParam.builder()
                    .name("_id")
                    .type("token")
                    .documentation("Buscar paciente por ID interno del sistema")
                    .build()
            ))
            .build();
    }

    private FhirCapabilityStatement.Resource buildEncounterCapability() {
        return FhirCapabilityStatement.Resource.builder()
            .type("Encounter")
            .profile("http://hl7.org/fhir/StructureDefinition/Encounter")
            .interaction(List.of(
                FhirCapabilityStatement.Interaction.builder()
                    .code("read")
                    .documentation("GET /api/fhir/Encounter/{id}")
                    .build(),
                FhirCapabilityStatement.Interaction.builder()
                    .code("search-type")
                    .documentation("GET /api/fhir/Encounter?patient=Patient/{id}&status={status}")
                    .build()
            ))
            .searchParam(List.of(
                FhirCapabilityStatement.SearchParam.builder()
                    .name("patient")
                    .type("reference")
                    .documentation("Filtrar encuentros por paciente. Ej: ?patient=Patient/5")
                    .build(),
                FhirCapabilityStatement.SearchParam.builder()
                    .name("status")
                    .type("token")
                    .documentation("Filtrar por estado FHIR: planned | arrived | finished | cancelled")
                    .build()
            ))
            .build();
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    private String nowFhir() {
        return LocalDateTime.now(ZoneOffset.UTC).format(FHIR_DATETIME);
    }
}
