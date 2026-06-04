package com.demo.ecosalud.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.mapper.AppointmentMapper;
import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.model.entities.Catalog;
import com.demo.ecosalud.model.entities.Therapist;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.CatalogRepository;
import com.demo.ecosalud.repository.TherapistRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.AppointmentService;

/**
 * Implementación del servicio de gestión de citas médicas.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final TherapistRepository therapistRepository;
    private final CatalogRepository catalogRepository;

    /**
     * Agenda una nueva cita validando todas las reglas de negocio:
     * <ul>
     *   <li>El paciente debe existir.</li>
     *   <li>El terapeuta debe existir y estar disponible.</li>
     *   <li>El servicio del catálogo debe existir y estar activo.</li>
     *   <li>El paciente no debe tener otra cita activa en la misma fecha y hora.</li>
     *   <li>El terapeuta no debe tener otra cita activa en la misma fecha y hora.</li>
     * </ul>
     *
     * @param appointmentDTO datos de la cita enviados por el frontend
     * @return cita creada en estado PENDIENTE
     */
    @Override
    public AppointmentDTO scheduleAppointment(AppointmentDTO appointmentDTO) {
        User user = userRepository.findById(appointmentDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente no encontrado con id: " + appointmentDTO.getUserId()));

        Therapist therapist = therapistRepository.findById(appointmentDTO.getTherapistId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Terapeuta no encontrado con id: " + appointmentDTO.getTherapistId()));

        if (!therapist.getAvailable()) {
            throw new RuntimeException("El terapeuta seleccionado no está disponible actualmente");
        }

        Catalog catalog = catalogRepository.findById(appointmentDTO.getCatalogId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Servicio no encontrado con id: " + appointmentDTO.getCatalogId()));

        if (!catalog.getAvailability()) {
            throw new RuntimeException("El servicio seleccionado no está disponible actualmente");
        }

        // Verificar que el paciente no tenga ya una cita activa en esa fecha y hora
        boolean userHasConflict = appointmentRepository.existsByUserIdAndDateAndStatusNot(
                user.getId(), appointmentDTO.getDate(), AppointmentSatus.CANCELADA);
        if (userHasConflict) {
            throw new RuntimeException("El paciente ya tiene una cita agendada en esa fecha y hora");
        }

        // Verificar que el terapeuta no tenga ya una cita activa en esa fecha y hora
        boolean therapistHasConflict = appointmentRepository.existsByTherapistIdAndDateAndStatusNot(
                therapist.getId(), appointmentDTO.getDate(), AppointmentSatus.CANCELADA);
        if (therapistHasConflict) {
            throw new RuntimeException("El terapeuta no tiene disponibilidad en la fecha y hora seleccionada");
        }

        Appointment appointment = AppointmentMapper.toEntity(appointmentDTO, user, therapist, catalog);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDTO(saved);
    }

    /**
     * Obtiene una cita por su ID.
     */
    @Override
    public AppointmentDTO getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(AppointmentMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));
    }

    /**
     * Retorna todas las citas de un paciente, útil para mostrar el historial en el frontend.
     */
    @Override
    public List<AppointmentDTO> getAppointmentsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Paciente no encontrado con id: " + userId);
        }
        return appointmentRepository.findByUserId(userId)
                .stream()
                .map(AppointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas las citas asignadas a un terapeuta.
     */
    @Override
    public List<AppointmentDTO> getAppointmentsByTherapist(Long therapistId) {
        if (!therapistRepository.existsById(therapistId)) {
            throw new ResourceNotFoundException("Terapeuta no encontrado con id: " + therapistId);
        }
        return appointmentRepository.findByTherapistId(therapistId)
                .stream()
                .map(AppointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas las citas filtradas por estado.
     */
    @Override
    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentSatus status) {
        return appointmentRepository.findByStatus(status)
                .stream()
                .map(AppointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todas las citas del sistema (uso administrativo).
     */
    @Override
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Reprograma una cita a una nueva fecha y hora.
     * No se puede reprogramar una cita cancelada.
     * Valida que el terapeuta siga disponible en la nueva fecha.
     * El estado cambia a REPROGRAMADA para mantener trazabilidad.
     *
     * @param id      ID de la cita a reprogramar
     * @param newDate nueva fecha y hora propuesta
     * @return cita actualizada en estado REPROGRAMADA
     */
    @Override
    public AppointmentDTO rescheduleAppointment(Long id, LocalDateTime newDate) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));

        if (appointment.getStatus() == AppointmentSatus.CANCELADA) {
            throw new RuntimeException("No se puede reprogramar una cita cancelada");
        }

        if (newDate.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La nueva fecha debe ser en el futuro");
        }

        // Verificar que el paciente esté libre en la nueva fecha
        boolean userHasConflict = appointmentRepository.existsByUserIdAndDateAndStatusNot(
                appointment.getUser().getId(), newDate, AppointmentSatus.CANCELADA);
        if (userHasConflict) {
            throw new RuntimeException("El paciente ya tiene una cita agendada en esa fecha y hora");
        }

        // Verificar que el terapeuta esté libre en la nueva fecha
        boolean therapistHasConflict = appointmentRepository.existsByTherapistIdAndDateAndStatusNot(
                appointment.getTherapist().getId(), newDate, AppointmentSatus.CANCELADA);
        if (therapistHasConflict) {
            throw new RuntimeException("El terapeuta no tiene disponibilidad en la nueva fecha y hora");
        }

        appointment.setDate(newDate);
        appointment.setStatus(AppointmentSatus.REPROGRAMADA);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDTO(saved);
    }

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     * No se puede cancelar una cita que ya esté cancelada.
     */
    @Override
    public AppointmentDTO cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));

        if (appointment.getStatus() == AppointmentSatus.CANCELADA) {
            throw new RuntimeException("La cita ya se encuentra cancelada");
        }

        appointment.setStatus(AppointmentSatus.CANCELADA);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDTO(saved);
    }

    /**
     * Confirma una cita cambiando su estado a CONFIRMADA.
     * No se puede confirmar una cita cancelada.
     */
    @Override
    public AppointmentDTO confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con id: " + id));

        if (appointment.getStatus() == AppointmentSatus.CANCELADA) {
            throw new RuntimeException("No se puede confirmar una cita cancelada");
        }

        appointment.setStatus(AppointmentSatus.CONFIRMADA);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toDTO(saved);
    }

    /**
     * Elimina permanentemente una cita de la base de datos (solo para admins).
     */
    @Override
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cita no encontrada con id: " + id);
        }
        appointmentRepository.deleteById(id);
    }
}
