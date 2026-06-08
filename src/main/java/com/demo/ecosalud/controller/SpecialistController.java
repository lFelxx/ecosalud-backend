package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.SpecialistDTO;
import com.demo.ecosalud.service.SpecialistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el perfil del especialista de la clínica.
 *
 * <p>Ruta base: {@code /api/specialist}</p>
 * <ul>
 *   <li>GET /api/specialist — obtener perfil (público)</li>
 *   <li>PUT /api/specialist — upsert perfil (JWT requerido)</li>
 * </ul>
 *
 * <p>Cada tenant tiene un único especialista principal; GET devuelve
 * siempre un objeto (vacío si aún no existe).</p>
 */
@RestController
@RequestMapping("/api/specialist")
@RequiredArgsConstructor
public class SpecialistController {

    private final SpecialistService service;

    /** Devuelve el perfil del especialista principal del tenant. */
    @GetMapping
    public SpecialistDTO get() {
        return service.get();
    }

    /** Crea o actualiza el perfil del especialista (upsert). */
    @PutMapping
    public SpecialistDTO upsert(@RequestBody SpecialistDTO dto) {
        return service.upsert(dto);
    }
}
