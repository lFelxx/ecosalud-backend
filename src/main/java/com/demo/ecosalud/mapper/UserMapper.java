package com.demo.ecosalud.mapper;

import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.model.entities.User;

public class UserMapper {
    
    public static User toEntity(UserDTO dto){

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        return user;
    }

    public static UserDTO toDTO(User user){

        UserDTO dto = new UserDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
