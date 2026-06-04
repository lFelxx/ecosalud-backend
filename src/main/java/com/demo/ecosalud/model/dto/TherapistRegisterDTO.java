package com.demo.ecosalud.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para el registro unificado de un terapeuta.
 * Combina los datos de cuenta (User) con el perfil profesional (Therapist) en una sola petición.
 * Los datos del perfil son opcionales: el terapeuta puede completarlos después.
 */
@Data
public class TherapistRegisterDTO {

    // ── Datos de cuenta (obligatorios) ──────────────────────

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // ── Datos del perfil profesional (opcionales al registro) ──

    private String specialty;

    private String biography;

    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    private Integer yearsOfExperience;
}
