package com.demo.ecosalud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.service.PlanLimitsService;
import com.demo.ecosalud.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
@RequiredArgsConstructor
@RestController
@RequestMapping("api/user")
@Tag(name = "Usuarios", description = "Registro y consulta de usuarios pacientes")
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

    /**
     * Obtiene los datos de un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return datos del usuario encontrado
     */
    @Operation(summary = "Obtener usuario por ID")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public UserDTO getUserById(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        return userService.getUserById(id);
    }
}
