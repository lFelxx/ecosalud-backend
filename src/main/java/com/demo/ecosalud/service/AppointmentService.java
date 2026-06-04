package com.demo.ecosalud.service;

import java.time.LocalDateTime;
import java.util.List;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.model.dto.AppointmentDTO;

/**
 * Contrato de negocio para la gestión de citas médicas.
 */
public interface AppointmentService {

    /**
     * Agenda una nueva cita validando disponibilidad del terapeuta y del servicio.
     *
     * @param appointmentDTO datos de la cita (paciente, terapeuta, servicio, fecha)
     * @return cita creada en estado PENDIENTE
     */
    AppointmentDTO scheduleAppointment(AppointmentDTO appointmentDTO);

    /**
     * Obtiene una cita por su ID.
     *
     * @param id identificador de la cita
     * @return datos de la cita
     */
    AppointmentDTO getAppointmentById(Long id);

    /**
     * Retorna todas las citas de un paciente.
     *
     * @param userId ID del paciente
     * @return lista de citas del paciente
     */
    List<AppointmentDTO> getAppointmentsByUser(Long userId);

    /**
     * Retorna todas las citas asignadas a un terapeuta.
     *
     * @param therapistId ID del perfil del terapeuta
     * @return lista de citas del terapeuta
     */
    List<AppointmentDTO> getAppointmentsByTherapist(Long therapistId);

    /**
     * Retorna todas las citas filtradas por estado.
     *
     * @param status estado a filtrar (PENDIENTE, CONFIRMADA, CANCELADA, REPROGRAMADA)
     * @return lista de citas con ese estado
     */
    List<AppointmentDTO> getAppointmentsByStatus(AppointmentSatus status);

    /**
     * Retorna todas las citas del sistema (uso administrativo).
     *
     * @return lista completa de citas
     */
    List<AppointmentDTO> getAllAppointments();

    /**
     * Reprograma una cita a una nueva fecha y hora.
     *
     * @param id      ID de la cita a reprogramar
     * @param newDate nueva fecha y hora propuesta
     * @return cita en estado REPROGRAMADA
     */
    AppointmentDTO rescheduleAppointment(Long id, LocalDateTime newDate);

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     *
     * @param id ID de la cita a cancelar
     * @return cita en estado CANCELADA
     */
    AppointmentDTO cancelAppointment(Long id);

    /**
     * Confirma una cita cambiando su estado a CONFIRMADA.
     *
     * @param id ID de la cita a confirmar
     * @return cita en estado CONFIRMADA
     */
    AppointmentDTO confirmAppointment(Long id);

    /**
     * Elimina permanentemente una cita (solo para admins).
     *
     * @param id ID de la cita a eliminar
     */
    void deleteAppointment(Long id);
}
