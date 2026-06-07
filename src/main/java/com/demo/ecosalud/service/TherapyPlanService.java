package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.TherapyPlanDTO;

import java.util.List;

/** Contrato del servicio de planes de terapia. */
public interface TherapyPlanService {

    List<TherapyPlanDTO> getAll();

    /** Retorna los planes de terapia de un paciente específico. */
    List<TherapyPlanDTO> getByPatientId(Long patientId);

    TherapyPlanDTO getById(Long id);

    TherapyPlanDTO create(TherapyPlanDTO dto);

    TherapyPlanDTO update(Long id, TherapyPlanDTO dto);

    void delete(Long id);
}
