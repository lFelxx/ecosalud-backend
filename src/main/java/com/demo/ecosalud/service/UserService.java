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

    /** Obtiene un usuario por su ID o lanza ResourceNotFoundException. */
    UserDTO getUserById(Long id);

    /** Actualiza los datos de un usuario existente. */
    UserDTO updateUser(Long id, UserDTO userDTO);

    /** Elimina un usuario del sistema. */
    void deleteUser(Long id);
}
