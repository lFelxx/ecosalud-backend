package com.demo.ecosalud.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta tras un login exitoso. Contiene el token JWT y datos básicos del usuario.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

    private String token;
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;

}
