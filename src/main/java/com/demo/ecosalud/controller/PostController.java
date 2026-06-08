package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.PostDTO;
import com.demo.ecosalud.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para publicaciones / blog de la clínica.
 *
 * <p>Ruta base: {@code /api/posts}</p>
 * <ul>
 *   <li>GET  /api/posts            — todas las publicaciones (JWT)</li>
 *   <li>GET  /api/posts/published  — solo publicadas (público)</li>
 *   <li>GET  /api/posts/{id}       — detalle</li>
 *   <li>POST /api/posts            — crear</li>
 *   <li>PUT  /api/posts/{id}       — actualizar</li>
 *   <li>DELETE /api/posts/{id}     — eliminar</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService service;

    /** Lista todas las publicaciones (admin panel). */
    @GetMapping
    public List<PostDTO> list() {
        return service.getAll();
    }

    /** Lista solo publicaciones publicadas (sitio público). */
    @GetMapping("/published")
    public List<PostDTO> listPublished() {
        return service.getPublished();
    }

    @GetMapping("/{id}")
    public PostDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDTO create(@RequestBody PostDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public PostDTO update(@PathVariable Long id, @RequestBody PostDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
