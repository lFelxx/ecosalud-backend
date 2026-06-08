package com.demo.ecosalud.mapper;

import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.model.entities.User;

/**
 * Convierte entre la entidad {@link com.demo.ecosalud.model.entities.User} y su {@link UserDTO}.
 */
public class UserMapper {

    public static User toEntity(UserDTO dto) {

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        return user;
    }

    public static UserDTO toDTO(User user) {

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        // Contraseña omitida intencionalmente en respuestas
        return dto;
    }
}
