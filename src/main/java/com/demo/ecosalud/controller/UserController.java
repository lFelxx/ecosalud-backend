package com.demo.ecosalud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de usuarios.
 *
 * <p>Ruta base: {@code /api/user}</p>
 * <ul>
 *   <li>POST   /register — Registro (público)</li>
 *   <li>GET    /{id}     — Consultar usuario</li>
 *   <li>PUT    /{id}     — Actualizar usuario</li>
 *   <li>DELETE /{id}     — Eliminar usuario</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Registra un nuevo usuario; devuelve el DTO con el ID generado. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO registrarUsuario(@Valid @RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    /** Busca y retorna un usuario por su ID. */
    @GetMapping("/{id}")
    public UserDTO obtenerUsuario(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /** Actualiza los datos de un usuario existente. */
    @PutMapping("/{id}")
    public UserDTO actualizarUsuario(@PathVariable Long id,
                                     @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    /** Elimina un usuario. Retorna 204 No Content si fue exitoso. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
