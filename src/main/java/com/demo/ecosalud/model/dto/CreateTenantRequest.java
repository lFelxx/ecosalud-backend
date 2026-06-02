package com.demo.ecosalud.model.dto;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.SubscriptionPlan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Solicitud para registrar una nueva clínica en la plataforma.
 * Solo el super-administrador puede crear tenants.
 */
@Data
public class CreateTenantRequest {

    @NotBlank(message = "El nombre de la clínica es obligatorio")
    private String name;

    /**
     * Slug único: solo minúsculas, números y guiones.
     * Ejemplo: {@code fisiosalud}, {@code ecosalud-camacho}
     */
    @NotBlank(message = "El slug es obligatorio")
    @Pattern(
        regexp  = "^[a-z0-9]+(-[a-z0-9]+)*$",
        message = "El slug solo puede contener letras minúsculas, números y guiones"
    )
    private String slug;

    @NotBlank(message = "El nombre del propietario es obligatorio")
    private String ownerName;

    @NotBlank(message = "El email del propietario es obligatorio")
    @Email(message = "Email inválido")
    private String ownerEmail;

    private String phone;
    private String address;
    private String city;
    private String country;
    private String specialty;

    /** Color primario de la marca. Default: {@code #3DAA96} */
    private String primaryColor;

    /** Plan inicial asignado a la clínica. */
    private SubscriptionPlan plan;

    /** Tipo de cuenta — REGULAR por defecto, FOUNDER para clínicas fundadoras. */
    private AccountType accountType;

    /** Código promocional (opcional). */
    private String promoCode;
}
