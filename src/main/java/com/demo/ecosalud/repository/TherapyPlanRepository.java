package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.TherapyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TherapyPlanRepository extends JpaRepository<TherapyPlan, Long> {

    /** Planes de terapia de un paciente, más recientes primero. */
    List<TherapyPlan> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /** Planes activos (para mostrar en el dashboard). */
    List<TherapyPlan> findByStatus(String status);
}
