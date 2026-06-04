package com.demo.ecosalud.mapper;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.model.entities.Catalog;
import com.demo.ecosalud.model.entities.Therapist;
import com.demo.ecosalud.model.entities.User;

/**
 * Convierte entre la entidad {@link Appointment} y su {@link AppointmentDTO}.
 */
public class AppointmentMapper {

    /**
     * Convierte un DTO de creación a entidad Appointment.
     * El estado inicial siempre es {@code CONFIRMADA}; el horario queda bloqueado en plataforma.
     *
     * @param dto       datos de la cita enviados por el frontend
     * @param user      paciente que agenda la cita
     * @param therapist terapeuta asignado para atender
     * @param catalog   servicio del catálogo seleccionado
     * @return entidad Appointment lista para persistir
     */
    public static Appointment toEntity(AppointmentDTO dto, User user, Therapist therapist, Catalog catalog) {
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setTherapist(therapist);
        appointment.setCatalog(catalog);
        appointment.setDate(dto.getDate());
        appointment.setStatus(AppointmentSatus.CONFIRMADA);
        return appointment;
    }

    /**
     * Convierte una entidad Appointment a DTO de respuesta.
     * Incluye nombres del paciente, terapeuta y servicio para evitar
     * llamadas adicionales desde el frontend.
     *
     * @param appointment entidad a convertir
     * @return DTO listo para serializar como respuesta JSON
     */
    public static AppointmentDTO toDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setTherapistId(appointment.getTherapist().getId());
        dto.setCatalogId(appointment.getCatalog().getId());
        dto.setDate(appointment.getDate());
        dto.setStatus(appointment.getStatus());
        dto.setUserName(appointment.getUser().getName());
        dto.setTherapistName(appointment.getTherapist().getUser().getName());
        dto.setCatalogName(appointment.getCatalog().getName());
        return dto;
    }
}
