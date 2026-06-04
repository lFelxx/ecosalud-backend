package com.demo.ecosalud.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para crear, actualizar y retornar datos del perfil de un terapeuta.
 */
@Data
public class TherapistDTO {

    private Long id;

    private Long userId;

    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;

    private String biography;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    private Integer yearsOfExperience;

    private Boolean available;

    // Campos de respuesta: datos básicos del usuario para no hacer llamadas adicionales
    private String userName;
    private String userEmail;
}
