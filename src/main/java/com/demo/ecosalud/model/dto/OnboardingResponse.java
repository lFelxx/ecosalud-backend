package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Respuesta al registro exitoso de una nueva clínica.
 *
 * <p>Incluye el JWT del admin para que el frontend pueda hacer login automático
 * sin redirigir al usuario a la página de login.</p>
 */
@Data
@Builder
public class OnboardingResponse {

    /** JWT listo para usar — expira según configuración (jwt.expiration). */
    private String token;

    /** ID del usuario administrador creado. */
    private Long userId;

    /** Nombre del administrador. */
    private String userName;

    /** Email del administrador (también su username de login). */
    private String userEmail;

    /** Rol asignado — siempre ADMIN en este flujo. */
    private String role;

    /** Subdominio generado — ej. "mi-clinica.ecosalud.com". */
    private String subdomain;

    /** Nombre de la clínica creada. */
    private String clinicName;
}
