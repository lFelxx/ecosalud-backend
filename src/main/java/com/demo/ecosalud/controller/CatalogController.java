package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.CatalogDTO;
import com.demo.ecosalud.service.CatalogService;
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
 * Controlador REST para la gestión del catálogo de servicios.
 * Base URL: {@code /api/catalog}
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catálogo", description = "Gestión del catálogo de servicios ofrecidos por la clínica")
@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "Crear servicio", description = "Agrega un nuevo servicio al catálogo")
    @ApiResponse(responseCode = "200", description = "Servicio creado")
    @ApiResponse(responseCode = "409", description = "Ya existe un servicio con ese nombre")
    @PostMapping
    public CatalogDTO createCatalog(@Valid @RequestBody CatalogDTO catalogDTO) {
        return catalogService.createCatalog(catalogDTO);
    }

    @Operation(summary = "Obtener servicio por ID")
    @ApiResponse(responseCode = "200", description = "Servicio encontrado")
    @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    @GetMapping("/{id}")
    public CatalogDTO getCatalogById(@Parameter(description = "ID del servicio") @PathVariable Long id) {
        return catalogService.getCatalogById(id);
    }

    @Operation(summary = "Listar servicios", description = "Retorna todos los servicios o filtra por categoría")
    @GetMapping
    public List<CatalogDTO> getCatalogs(
            @Parameter(description = "Categoría para filtrar (opcional)") @RequestParam(required = false) String category) {
        return catalogService.getCatalogs(category);
    }

    @Operation(summary = "Listar servicios disponibles", description = "Retorna solo los servicios activos, opcionalmente por categoría")
    @GetMapping("/available")
    public List<CatalogDTO> getAvailableCatalogs(
            @Parameter(description = "Categoría para filtrar (opcional)") @RequestParam(required = false) String category) {
        return catalogService.getAvailableCatalogs(category);
    }

    @Operation(summary = "Actualizar servicio")
    @ApiResponse(responseCode = "200", description = "Servicio actualizado")
    @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    @ApiResponse(responseCode = "409", description = "Nombre duplicado")
    @PutMapping("/{id}")
    public CatalogDTO updateCatalog(
            @Parameter(description = "ID del servicio") @PathVariable Long id,
            @Valid @RequestBody CatalogDTO catalogDTO) {
        return catalogService.updateCatalog(id, catalogDTO);
    }

    @Operation(summary = "Cambiar disponibilidad", description = "Activa o desactiva un servicio del catálogo")
    @PutMapping("/{id}/toggle-availability")
    public CatalogDTO toggleAvailability(@Parameter(description = "ID del servicio") @PathVariable Long id) {
        return catalogService.toggleAvailability(id);
    }

    @Operation(summary = "Eliminar servicio")
    @ApiResponse(responseCode = "200", description = "Servicio eliminado")
    @ApiResponse(responseCode = "404", description = "Servicio no encontrado")
    @DeleteMapping("/{id}")
    public void deleteCatalog(@Parameter(description = "ID del servicio") @PathVariable Long id) {
        catalogService.deleteCatalog(id);
    }
}
