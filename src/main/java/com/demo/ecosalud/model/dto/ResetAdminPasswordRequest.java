package com.demo.ecosalud.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para que el super-admin restablezca la contraseña del administrador de una clínica.
 *
 * <p>Flujo de soporte típico:</p>
 * <ol>
 *   <li>Cliente llama al soporte porque olvidó su contraseña.</li>
 *   <li>Super-admin verifica identidad y usa este endpoint.</li>
 *   <li>Cliente recibe contraseña temporal y la cambia en su primer acceso.</li>
 * </ol>
 */
@Data
public class ResetAdminPasswordRequest {

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String newPassword;
}
