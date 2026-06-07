package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.TherapyPlanDTO;
import com.demo.ecosalud.service.TherapyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para planes de terapia multi-sesión.
 *
 * <p>Ruta base: {@code /api/therapy-plans}</p>
 */
@RestController
@RequestMapping("/api/therapy-plans")
@RequiredArgsConstructor
public class TherapyPlanController {

    private final TherapyPlanService service;

    /**
     * Lista planes de terapia. Con {@code ?patientId=N} filtra por paciente.
     */
    @GetMapping
    public List<TherapyPlanDTO> list(@RequestParam(required = false) Long patientId) {
        return patientId != null ? service.getByPatientId(patientId) : service.getAll();
    }

    @GetMapping("/{id}")
    public TherapyPlanDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TherapyPlanDTO create(@RequestBody TherapyPlanDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public TherapyPlanDTO update(@PathVariable Long id, @RequestBody TherapyPlanDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
