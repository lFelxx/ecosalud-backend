package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.HealthRecordDTO;
import com.demo.ecosalud.model.entities.HealthRecord;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.HealthRecordRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.HealthRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de historia clínica electrónica.
 *
 * <h3>Reglas de negocio — Resolución 1995/1999 de MinSalud</h3>
 * <ul>
 *   <li><strong>Campos obligatorios:</strong> motivo de consulta y diagnóstico.</li>
 *   <li><strong>Inmutabilidad:</strong> una historia bloqueada ({@code is_locked = true})
 *       no puede ser editada ni eliminada.</li>
 *   <li><strong>Integridad:</strong> hash SHA-256 calculado sobre los campos clínicos;
 *       se regenera en cada escritura para detectar manipulaciones.</li>
 *   <li><strong>Custodia 20 años:</strong> pendiente Fase 3 — política de retención
 *       en la capa de almacenamiento.</li>
 * </ul>
 *
 * <p>A través del multi-tenancy de Hibernate, todas las operaciones de repositorio
 * se dirigen al schema del tenant activo (ej. {@code tenant_dra_angelica_camacho}).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordRepository repo;
    private final UserRepository         userRepo;

    // ── Lectura ───────────────────────────────────────────────────────────────

    @Override
    public List<HealthRecordDTO> getAll() {
        return repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HealthRecordDTO> getByPatient(Long patientId) {
        return repo.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HealthRecordDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    @Override
    public HealthRecordDTO create(HealthRecordDTO dto) {
        validateRequired(dto);

        HealthRecord rec = new HealthRecord();
        copyFields(dto, rec);
        rec.setCreatedAt(LocalDateTime.now());
        rec.setHashIntegrity(computeHash(rec));

        HealthRecord saved = repo.save(rec);
        log.info("Historia clínica creada — id={}, paciente={}", saved.getId(), saved.getPatientId());
        return toDTO(saved);
    }

    @Override
    public HealthRecordDTO update(Long id, HealthRecordDTO dto) {
        HealthRecord rec = findOrThrow(id);

        if (rec.isLocked()) {
            throw new IllegalStateException(
                    "La historia clínica #" + id + " está bloqueada y no puede ser modificada (Res. 1995/1999).");
        }

        validateRequired(dto);
        copyFields(dto, rec);
        rec.setUpdatedAt(LocalDateTime.now());
        rec.setHashIntegrity(computeHash(rec));

        log.info("Historia clínica actualizada — id={}", id);
        return toDTO(repo.save(rec));
    }

    @Override
    public HealthRecordDTO lock(Long id) {
        HealthRecord rec = findOrThrow(id);

        if (rec.isLocked()) {
            // Idempotente: si ya está bloqueada, retorna sin error
            return toDTO(rec);
        }

        rec.setLocked(true);
        rec.setUpdatedAt(LocalDateTime.now());
        log.info("Historia clínica bloqueada — id={}", id);
        return toDTO(repo.save(rec));
    }

    @Override
    public void delete(Long id) {
        HealthRecord rec = findOrThrow(id);

        if (rec.isLocked()) {
            throw new IllegalStateException(
                    "La historia clínica #" + id + " está bloqueada y no puede ser eliminada (Res. 1995/1999).");
        }

        repo.deleteById(id);
        log.info("Historia clínica eliminada — id={}", id);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private HealthRecord findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Historia clínica no encontrada con id: " + id));
    }

    /**
     * Valida los campos mínimos exigidos por la Res. 1995/1999.
     * Lanza {@link IllegalArgumentException} si alguno falta.
     */
    private void validateRequired(HealthRecordDTO dto) {
        if (dto.getPatientId() == null) {
            throw new IllegalArgumentException("El paciente es obligatorio.");
        }
        if (isBlank(dto.getReasonForConsultation())) {
            throw new IllegalArgumentException(
                    "El motivo de consulta es obligatorio (Res. 1995/1999).");
        }
        if (isBlank(dto.getDiagnosis())) {
            throw new IllegalArgumentException(
                    "El diagnóstico es obligatorio (Res. 1995/1999).");
        }
    }

    /** Copia los campos editables del DTO a la entidad. */
    private void copyFields(HealthRecordDTO dto, HealthRecord rec) {
        rec.setPatientId(dto.getPatientId());
        rec.setAppointmentId(dto.getAppointmentId());
        rec.setSpecialistId(dto.getSpecialistId());
        rec.setReasonForConsultation(dto.getReasonForConsultation());
        rec.setCurrentIllness(dto.getCurrentIllness());
        rec.setPhysicalExam(dto.getPhysicalExam());
        rec.setDiagnosis(dto.getDiagnosis());
        rec.setTreatmentPlan(dto.getTreatmentPlan());
        rec.setObservations(dto.getObservations());
        rec.setNextAppointment(dto.getNextAppointment());
        rec.setCreatedBy(dto.getCreatedBy());
    }

    /** Convierte la entidad a DTO, resolviendo el nombre del paciente. */
    private HealthRecordDTO toDTO(HealthRecord rec) {
        HealthRecordDTO dto = new HealthRecordDTO();
        dto.setId(rec.getId());
        dto.setPatientId(rec.getPatientId());

        // Resuelve el nombre del paciente en el schema activo del tenant
        String name = userRepo.findById(rec.getPatientId())
                .map(User::getName)
                .orElse("Paciente #" + rec.getPatientId());
        dto.setPatientName(name);

        dto.setAppointmentId(rec.getAppointmentId());
        dto.setSpecialistId(rec.getSpecialistId());
        dto.setReasonForConsultation(rec.getReasonForConsultation());
        dto.setCurrentIllness(rec.getCurrentIllness());
        dto.setPhysicalExam(rec.getPhysicalExam());
        dto.setDiagnosis(rec.getDiagnosis());
        dto.setTreatmentPlan(rec.getTreatmentPlan());
        dto.setObservations(rec.getObservations());
        dto.setNextAppointment(rec.getNextAppointment());
        dto.setLocked(rec.isLocked());
        dto.setHashIntegrity(rec.getHashIntegrity());
        dto.setCreatedBy(rec.getCreatedBy());
        dto.setCreatedAt(rec.getCreatedAt());
        dto.setUpdatedAt(rec.getUpdatedAt());
        return dto;
    }

    /**
     * Genera un hash SHA-256 de los campos clínicos principales.
     * El hash detecta cualquier manipulación directa en la base de datos.
     *
     * <p>Formato del mensaje: {@code patientId|motivo|anamnesis|examenFisico|diagnostico|tratamiento|observaciones}</p>
     */
    private String computeHash(HealthRecord rec) {
        String message = String.join("|",
                safe(String.valueOf(rec.getPatientId())),
                safe(rec.getReasonForConsultation()),
                safe(rec.getCurrentIllness()),
                safe(rec.getPhysicalExam()),
                safe(rec.getDiagnosis()),
                safe(rec.getTreatmentPlan()),
                safe(rec.getObservations()));

        try {
            MessageDigest md  = MessageDigest.getInstance("SHA-256");
            byte[]        raw = md.digest(message.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 no disponible — hash de integridad no generado", e);
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
