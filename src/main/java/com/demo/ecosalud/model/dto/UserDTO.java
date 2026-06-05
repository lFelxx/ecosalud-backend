package com.demo.ecosalud.model.dto;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;

import lombok.Data;

/**
 * DTO para crear y retornar datos de un usuario paciente.
 */
@Data
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String password;
    private RolUser role;
    private UserStatus status;

}
