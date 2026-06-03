package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.UserDTO;

public interface UserService {

    /**
     * Registra un usuario nuevo en el sistema.
     */
    UserDTO register(UserDTO userDTO);

    /**
     * Obtiene un usuario por su id.
     * Mantiene la separación de capas: controlador -> servicio -> repositorio.
     */
    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);
}
