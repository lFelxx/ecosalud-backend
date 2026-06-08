package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para consentimientos informados.
 * Opera sobre la tabla {@code consents} del schema del tenant activo.
 */
public interface ConsentRepository extends JpaRepository<Consent, Long> {

    /** Retorna todos los consentimientos de un paciente, del más reciente al más antiguo. */
    List<Consent> findByPatientIdOrderBySignedAtDesc(Long patientId);

    /** Verifica si un paciente ya firmó el consentimiento para un servicio. */
    boolean existsByPatientIdAndServiceId(Long patientId, Long serviceId);
}
