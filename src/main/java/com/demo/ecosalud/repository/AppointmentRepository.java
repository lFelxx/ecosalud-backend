package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /** Citas de un paciente específico, ordenadas por fecha descendente. */
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

    /** Citas con un estado específico (PENDIENTE, CONFIRMADA, etc.). */
    List<Appointment> findByStatus(String status);

    /**
     * Retorna todas las citas de una fecha específica cuyo estado esté dentro de la colección.
     * Usado por {@code AppointmentReminderJob} para buscar citas de mañana que necesitan recordatorio.
     *
     * @param date     fecha exacta de la cita
     * @param statuses estados aceptados (p.ej. PENDIENTE, CONFIRMADA)
     */
    List<Appointment> findByAppointmentDateAndStatusIn(LocalDate date, Collection<String> statuses);

    /**
     * Retorna citas cuya fecha esté en el rango [from, to] (inclusivo) y estado sea uno de los dados.
     * Usado por el panel de notificaciones para mostrar los próximos recordatorios.
     */
    List<Appointment> findByAppointmentDateBetweenAndStatusInOrderByAppointmentDateAscAppointmentTimeAsc(
            LocalDate from, LocalDate to, Collection<String> statuses);

    /**
     * Cuenta citas cuya fecha de realización cae en el rango [start, end] (inclusivo).
     * Usado por {@code PlanLimitsService} para verificar el límite mensual de citas.
     *
     * @param start primer día del mes
     * @param end   último día del mes
     */
    long countByAppointmentDateBetween(LocalDate start, LocalDate end);
}
