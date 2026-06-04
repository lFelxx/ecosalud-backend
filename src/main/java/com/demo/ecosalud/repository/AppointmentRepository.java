package com.demo.ecosalud.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.model.entities.Appointment;

/**
 * Repositorio JPA para la entidad {@link Appointment}.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Retorna todas las citas de un paciente. */
    List<Appointment> findByUserId(Long userId);

    /** Retorna todas las citas asignadas a un terapeuta. */
    List<Appointment> findByTherapistId(Long therapistId);

    /** Retorna todas las citas filtradas por estado. */
    List<Appointment> findByStatus(AppointmentSatus status);

    /**
     * Detecta conflicto de horario para el paciente:
     * misma fecha, mismo paciente, excluyendo citas canceladas.
     */
    Boolean existsByUserIdAndDateAndStatusNot(Long userId, LocalDateTime date, AppointmentSatus status);

    /**
     * Detecta conflicto de horario para el terapeuta:
     * misma fecha, mismo terapeuta, excluyendo citas canceladas.
     */
    Boolean existsByTherapistIdAndDateAndStatusNot(Long therapistId, LocalDateTime date, AppointmentSatus status);
}
