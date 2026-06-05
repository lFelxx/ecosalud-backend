package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.UserDTO;
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

/**
 * Controlador REST para la gestión de usuarios pacientes.
 * Base URL: {@code /api/user}
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("api/user")
@Tag(name = "Usuarios", description = "Registro y consulta de usuarios pacientes")
public class UserController {

    private final UserService userService;

    /**
     * Registra un nuevo usuario paciente en el sistema.
     *
     * @param user datos del usuario a registrar
     * @return usuario creado con su ID asignado
     */
    @Operation(summary = "Registrar usuario", description = "Crea una cuenta de paciente. Endpoint público, no requiere token.")
    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "409", description = "Email o nombre ya registrado")
    @PostMapping("/register")
    public UserDTO userRegister(@RequestBody UserDTO user) {
        return userService.register(user);
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
