package com.demo.ecosalud.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.demo.ecosalud.model.dto.TherapistDTO;
import com.demo.ecosalud.model.dto.TherapistRegisterDTO;
import com.demo.ecosalud.service.TherapistService;

/**
 * Controlador REST para la gestión de perfiles de terapeutas.
 * Base URL: {@code /api/therapists}
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/therapists")
public class TherapistController {

    private final TherapistService therapistService;

    // ──────────────────────── CREATE ────────────────────────

    @PostMapping("/register")
    public TherapistDTO registerTherapist(@Valid @RequestBody TherapistRegisterDTO dto) {
        return therapistService.registerTherapist(dto);
    }

    // ──────────────────────── READ ──────────────────────────

    @GetMapping("/{id}")
    public TherapistDTO getTherapistById(@PathVariable Long id) {
        return therapistService.getTherapistById(id);
    }

    @GetMapping("/user/{userId}")
    public TherapistDTO getTherapistByUserId(@PathVariable Long userId) {
        return therapistService.getTherapistByUserId(userId);
    }

    @GetMapping
    public List<TherapistDTO> getAllTherapists() {
        return therapistService.getAllTherapists();
    }

    @GetMapping("/available")
    public List<TherapistDTO> getAvailableTherapists() {
        return therapistService.getAvailableTherapists();
    }

    @GetMapping("/specialty/{specialty}")
    public List<TherapistDTO> getTherapistsBySpecialty(@PathVariable String specialty) {
        return therapistService.getTherapistsBySpecialty(specialty);
    }

    // ──────────────────────── UPDATE ────────────────────────

    @PutMapping("/{id}")
    public TherapistDTO updateTherapist(@PathVariable Long id, @Valid @RequestBody TherapistDTO therapistDTO) {
        return therapistService.updateTherapist(id, therapistDTO);
    }

    @PutMapping("/{id}/toggle-availability")
    public TherapistDTO toggleAvailability(@PathVariable Long id) {
        return therapistService.toggleAvailability(id);
    }

    // ──────────────────────── DELETE ────────────────────────

    @DeleteMapping("/{id}")
    public void deleteTherapist(@PathVariable Long id) {
        therapistService.deleteTherapist(id);
    }
}
