package com.demo.ecosalud.mapper;

import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.model.entities.User;

/**
 * Clase utilitaria para convertir entre la entidad {@link User}
 * y el objeto de transferencia {@link UserDTO}.
 *
 * <p>La contraseña <strong>nunca</strong> se incluye en el DTO de respuesta
 * para evitar exponer el hash en la API.</p>
 */
public class UserMapper {

    private UserMapper() {
        // Clase utilitaria: no instanciar
    }

    /**
     * Convierte un DTO de entrada a entidad JPA.
     * La contraseña debe setearse por separado (con hash).
     */
    public static User toEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        return user;
    }

    /**
     * Convierte una entidad JPA a DTO de respuesta.
     * Omite la contraseña hasheada.
     */
    public static UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        // Contraseña omitida intencionalmente en respuestas
        return dto;
    }
}
