package com.demo.ecosalud.mapper;

import com.demo.ecosalud.model.dto.TherapistDTO;
import com.demo.ecosalud.model.entities.Therapist;

/**
 * Convierte entre la entidad {@link Therapist} y su {@link TherapistDTO}.
 */
public class TherapistMapper {

    /**
     * Convierte una entidad Therapist a DTO de respuesta.
     * Incluye nombre y email del usuario para que el frontend no haga llamadas adicionales.
     *
     * @param therapist entidad a convertir
     * @return DTO listo para serializar como respuesta JSON
     */
    public static TherapistDTO toDTO(Therapist therapist) {
        TherapistDTO dto = new TherapistDTO();
        dto.setId(therapist.getId());
        dto.setUserId(therapist.getUser().getId());
        dto.setSpecialty(therapist.getSpecialty());
        dto.setBiography(therapist.getBiography());
        dto.setYearsOfExperience(therapist.getYearsOfExperience());
        dto.setAvailable(therapist.getAvailable());
        dto.setUserName(therapist.getUser().getName());
        dto.setUserEmail(therapist.getUser().getEmail());
        return dto;
    }
}
