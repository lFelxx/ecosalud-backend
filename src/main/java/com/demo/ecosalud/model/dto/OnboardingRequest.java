package com.demo.ecosalud.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Solicitud de registro de nueva clínica desde la landing pública.
 *
 * <p>Difiere de {@link CreateTenantRequest} en que es público (sin JWT)
 * y no permite elegir {@code AccountType} (siempre REGULAR).</p>
 */
@Data
public class OnboardingRequest {

    @NotBlank(message = "El nombre de la clínica es obligatorio")
    private String clinicName;

    @NotBlank(message = "El slug es obligatorio")
    @Pattern(
        regexp  = "^[a-z0-9]+(-[a-z0-9]+)*$",
        message = "El slug solo puede contener letras minúsculas, números y guiones"
    )
    private String slug;

    @NotBlank(message = "El nombre del propietario es obligatorio")
    private String ownerName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String ownerEmail;

    private String phone;
    private String specialty;
    private String city;
    private String country;

    /** Plan elegido por el usuario: STARTER o PRO. */
    private String plan;
}
