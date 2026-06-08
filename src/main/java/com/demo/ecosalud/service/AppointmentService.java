package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.AppointmentDTO;

import java.util.List;

/** Contrato del servicio de citas médicas. */
public interface AppointmentService {

    List<AppointmentDTO> getAll();

    /** Retorna las citas de un paciente específico (para el dashboard del paciente). */
    List<AppointmentDTO> getByPatientId(Long patientId);

    AppointmentDTO getById(Long id);

    AppointmentDTO create(AppointmentDTO dto);

    AppointmentDTO update(Long id, AppointmentDTO dto);

    /** Cambia solo el estado de la cita (CONFIRMADA, COMPLETADA, CANCELADA…). */
    AppointmentDTO updateStatus(Long id, String status, String cancellationReason);

    void delete(Long id);
}
