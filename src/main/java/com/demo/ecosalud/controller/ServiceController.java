package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.ServiceDTO;
import com.demo.ecosalud.service.ClinicServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para servicios / terapias de la clínica.
 *
 * <p>Ruta base: {@code /api/services}</p>
 * <ul>
 *   <li>GET    /api/services       — lista todos (público)</li>
 *   <li>POST   /api/services       — crear (JWT requerido)</li>
 *   <li>PUT    /api/services/{id}  — actualizar (JWT requerido)</li>
 *   <li>DELETE /api/services/{id}  — eliminar (JWT requerido)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ClinicServiceService service;

    @GetMapping
    public List<ServiceDTO> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ServiceDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceDTO create(@RequestBody ServiceDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ServiceDTO update(@PathVariable Long id, @RequestBody ServiceDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
