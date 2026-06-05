package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.LoginRequestDTO;
import com.demo.ecosalud.model.dto.LoginResponseDTO;
import com.demo.ecosalud.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la autenticación de usuarios.
 * Proporciona el endpoint de login y emisión de tokens JWT.
 */
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para login y obtención del token JWT")
public class AuthController {

    private final AuthService authService;

    /**
     * Autentica un usuario y retorna un token JWT.
     *
     * @param request credenciales del usuario (email y contraseña)
     * @return token JWT junto con el email y rol del usuario autenticado
     */
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica al usuario con email y contraseña. Retorna un token JWT para usar en los demás endpoints."
    )
    @ApiResponse(responseCode = "200", description = "Login exitoso, token generado",
        content = @Content(schema = @Schema(implementation = LoginResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
