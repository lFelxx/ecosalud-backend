package com.demo.ecosalud.model.dto;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String password;
    private RolUser role;
    private UserStatus status;
    /** Fecha de creación del usuario — solo salida, nunca se lee en entrada. */
    private LocalDateTime createdAt;

}
