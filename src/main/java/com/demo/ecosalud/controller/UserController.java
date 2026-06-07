package com.demo.ecosalud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.service.PlanLimitsService;
import com.demo.ecosalud.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios del tenant.
 *
 * <p>Ruta base: {@code /api/user}</p>
 * <ul>
 *   <li>GET    /           — Lista todos los usuarios del tenant (JWT requerido)</li>
 *   <li>POST   /register   — Registro de usuario en el tenant (JWT requerido)</li>
 *   <li>GET    /{id}       — Consultar usuario por ID</li>
 *   <li>PUT    /{id}       — Actualizar usuario</li>
 *   <li>DELETE /{id}       — Eliminar usuario</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService      userService;
    private final PlanLimitsService planLimits;

    /** Lista todos los usuarios del tenant activo (pacientes, terapeutas, admins). */
    @GetMapping
    public List<UserDTO> listarUsuarios() {
        return userService.getAllUsers();
    }

    /** Registra un nuevo usuario en el tenant activo. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO registrarUsuario(@Valid @RequestBody UserDTO userDTO) {
        // Solo verifica el límite de pacientes para el rol USER;
        // terapeutas y administradores no cuentan contra este cupo.
        if (userDTO.getRole() == null || userDTO.getRole() == RolUser.USER) {
            planLimits.checkPatientLimit(); // 402 si se superó la cuota del plan
        }
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
