package com.demo.ecosalud.model.dto;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;

import lombok.Data;

@Data
public class UserDTO {

    private String name;
    private String email;
    private String password;
    private RolUser role;
    private UserStatus status;
    
}
