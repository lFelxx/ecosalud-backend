package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.TherapistDTO;
import com.demo.ecosalud.model.dto.TherapistRegisterDTO;
import com.demo.ecosalud.service.TherapistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de perfiles de terapeutas.
 * Base URL: {@code /api/therapists}
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/therapists")
@Tag(name = "Terapeutas", description = "Registro y gestión de perfiles de terapeutas")
public class TherapistController {

    private final TherapistService therapistService;

    @Operation(summary = "Registrar terapeuta", description = "Crea el usuario y el perfil profesional del terapeuta. Endpoint público.")
    @ApiResponse(responseCode = "200", description = "Terapeuta registrado")
    @ApiResponse(responseCode = "409", description = "Email o nombre ya registrado")
    @PostMapping("/register")
    public TherapistDTO registerTherapist(@Valid @RequestBody TherapistRegisterDTO dto) {
        return therapistService.registerTherapist(dto);
    }

    @Operation(summary = "Obtener terapeuta por ID de perfil")
    @ApiResponse(responseCode = "200", description = "Terapeuta encontrado")
    @ApiResponse(responseCode = "404", description = "Terapeuta no encontrado")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public TherapistDTO getTherapistById(@Parameter(description = "ID del perfil de terapeuta") @PathVariable Long id) {
        return therapistService.getTherapistById(id);
    }

    @Operation(summary = "Obtener terapeuta por ID de usuario")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/user/{userId}")
    public TherapistDTO getTherapistByUserId(@Parameter(description = "ID del usuario asociado") @PathVariable Long userId) {
        return therapistService.getTherapistByUserId(userId);
    }

    @Operation(summary = "Listar todos los terapeutas")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public List<TherapistDTO> getAllTherapists() {
        return therapistService.getAllTherapists();
    }

    @Operation(summary = "Listar terapeutas disponibles")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/available")
    public List<TherapistDTO> getAvailableTherapists() {
        return therapistService.getAvailableTherapists();
    }

    @Operation(summary = "Buscar por especialidad")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/specialty/{specialty}")
    public List<TherapistDTO> getTherapistsBySpecialty(
            @Parameter(description = "Especialidad a buscar") @PathVariable String specialty) {
        return therapistService.getTherapistsBySpecialty(specialty);
    }

    @Operation(summary = "Actualizar perfil de terapeuta")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "404", description = "Terapeuta no encontrado")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public TherapistDTO updateTherapist(
            @Parameter(description = "ID del perfil de terapeuta") @PathVariable Long id,
            @Valid @RequestBody TherapistDTO therapistDTO) {
        return therapistService.updateTherapist(id, therapistDTO);
    }

    @Operation(summary = "Cambiar disponibilidad del terapeuta")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/toggle-availability")
    public TherapistDTO toggleAvailability(@Parameter(description = "ID del perfil de terapeuta") @PathVariable Long id) {
        return therapistService.toggleAvailability(id);
    }

    @Operation(summary = "Eliminar terapeuta")
    @ApiResponse(responseCode = "200", description = "Terapeuta eliminado")
    @ApiResponse(responseCode = "404", description = "Terapeuta no encontrado")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public void deleteTherapist(@Parameter(description = "ID del perfil de terapeuta") @PathVariable Long id) {
        therapistService.deleteTherapist(id);
    }
}
