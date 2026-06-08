package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para {@link HealthRecord}.
 *
 * <p>A través del mecanismo multi-tenant de Hibernate, todas las consultas
 * se dirigen automáticamente al schema del tenant activo
 * (vía {@code TenantIdentifierResolver} / {@code TenantContext}).</p>
 */
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    /**
     * Historias clínicas de un paciente, ordenadas por fecha de creación descendente.
     * Útil para mostrar el historial cronológico del paciente.
     *
     * @param patientId ID del paciente en {@code tenant.users}
     */
    List<HealthRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * Verifica si ya existe una historia clínica vinculada a una cita específica.
     * Sirve para evitar duplicados al crear la historia desde el módulo de citas.
     *
     * @param appointmentId ID de la cita
     */
    boolean existsByAppointmentId(Long appointmentId);
}
