package com.demo.ecosalud.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.demo.ecosalud.model.dto.CatalogDTO;
import com.demo.ecosalud.service.CatalogService;

/**
 * Controlador REST para la gestión del catálogo de servicios.
 * Base URL: {@code /api/catalog}
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    // ──────────────────────── CREATE ────────────────────────

    @PostMapping
    public CatalogDTO createCatalog(@Valid @RequestBody CatalogDTO catalogDTO) {
        return catalogService.createCatalog(catalogDTO);
    }

    // ──────────────────────── READ ──────────────────────────

    @GetMapping("/{id}")
    public CatalogDTO getCatalogById(@PathVariable Long id) {
        return catalogService.getCatalogById(id);
    }

    @GetMapping
    public List<CatalogDTO> getCatalogs(@RequestParam(required = false) String category) {
        return catalogService.getCatalogs(category);
    }

    @GetMapping("/available")
    public List<CatalogDTO> getAvailableCatalogs(@RequestParam(required = false) String category) {
        return catalogService.getAvailableCatalogs(category);
    }

    // ──────────────────────── UPDATE ────────────────────────

    @PutMapping("/{id}")
    public CatalogDTO updateCatalog(@PathVariable Long id, @Valid @RequestBody CatalogDTO catalogDTO) {
        return catalogService.updateCatalog(id, catalogDTO);
    }

    @PutMapping("/{id}/toggle-availability")
    public CatalogDTO toggleAvailability(@PathVariable Long id) {
        return catalogService.toggleAvailability(id);
    }

    // ──────────────────────── DELETE ────────────────────────

    @DeleteMapping("/{id}")
    public void deleteCatalog(@PathVariable Long id) {
        catalogService.deleteCatalog(id);
    }
}
