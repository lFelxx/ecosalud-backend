package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.UserDTO;

import java.util.List;

/**
 * Contrato del servicio de usuarios.
 * Define las operaciones CRUD disponibles para la capa de negocio.
 */
public interface UserService {

    /** Lista todos los usuarios del tenant activo. */
    List<UserDTO> getAllUsers();

    /** Registra un usuario nuevo; valida duplicados de email. */
    UserDTO register(UserDTO userDTO);

    /**
     * Obtiene un usuario por su id.
     * Mantiene la separación de capas: controlador -> servicio -> repositorio.
     */
    UserDTO getUserById(Long id);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);
}
