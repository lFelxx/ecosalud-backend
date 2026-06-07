package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.HealthRecordDTO;

import java.util.List;

/**
 * Contrato del servicio de historia clínica electrónica.
 *
 * <p>Todas las operaciones aplican sobre el tenant activo determinado
 * por {@code TenantContext} (schema PostgreSQL del tenant).</p>
 *
 * <p>Cumple con la <strong>Resolución 1995/1999 de MinSalud</strong>:
 * campos obligatorios, bloqueo de registros e integridad por hash.</p>
 */
public interface HealthRecordService {

    /** Lista todas las historias clínicas del tenant activo. */
    List<HealthRecordDTO> getAll();

    /**
     * Lista las historias clínicas de un paciente, ordenadas de más reciente a más antigua.
     *
     * @param patientId ID del paciente en {@code tenant.users}
     */
    List<HealthRecordDTO> getByPatient(Long patientId);

    /**
     * Retorna una historia clínica por su ID.
     *
     * @throws com.demo.ecosalud.exception.ResourceNotFoundException si no existe
     */
    HealthRecordDTO getById(Long id);

    /**
     * Crea una nueva historia clínica.
     * Valida campos obligatorios y calcula el hash de integridad.
     *
     * @throws IllegalArgumentException si faltan campos obligatorios
     */
    HealthRecordDTO create(HealthRecordDTO dto);

    /**
     * Actualiza una historia clínica existente.
     *
     * @throws IllegalStateException     si la historia está bloqueada
     * @throws IllegalArgumentException  si faltan campos obligatorios
     */
    HealthRecordDTO update(Long id, HealthRecordDTO dto);

    /**
     * Bloquea una historia clínica, haciéndola inmutable.
     * Una vez bloqueada no puede editarse ni eliminarse (Res. 1995/1999).
     */
    HealthRecordDTO lock(Long id);

    /**
     * Elimina una historia clínica.
     *
     * @throws IllegalStateException si la historia está bloqueada
     */
    void delete(Long id);
}
