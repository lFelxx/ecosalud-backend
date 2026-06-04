package com.demo.ecosalud.service;

import java.util.List;

import com.demo.ecosalud.model.dto.CatalogDTO;

/**
 * Contrato de negocio para la gestión del catálogo de servicios.
 */
public interface CatalogService {

    /**
     * Crea un nuevo servicio en el catálogo.
     * Valida que el nombre no esté duplicado.
     *
     * @param catalogDTO datos del servicio a crear
     * @return servicio creado con su ID asignado
     */
    CatalogDTO createCatalog(CatalogDTO catalogDTO);

    /**
     * Obtiene un servicio del catálogo por su ID.
     *
     * @param id identificador del servicio
     * @return datos del servicio
     */
    CatalogDTO getCatalogById(Long id);

    /**
     * Retorna servicios del catálogo, opcionalmente filtrados por categoría.
     * Si {@code category} es null retorna todos (uso administrativo).
     *
     * @param category categoría a filtrar, o null para traer todos
     * @return lista de servicios
     */
    List<CatalogDTO> getCatalogs(String category);

    /**
     * Retorna servicios activos, opcionalmente filtrados por categoría.
     * Endpoint principal para el frontend.
     *
     * @param category categoría a filtrar, o null para traer todos los disponibles
     * @return lista de servicios con {@code availability = true}
     */
    List<CatalogDTO> getAvailableCatalogs(String category);

    /**
     * Actualiza los datos de un servicio existente.
     *
     * @param id         ID del servicio a actualizar
     * @param catalogDTO nuevos datos del servicio
     * @return servicio actualizado
     */
    CatalogDTO updateCatalog(Long id, CatalogDTO catalogDTO);

    /**
     * Invierte la disponibilidad de un servicio (activo ↔ inactivo).
     *
     * @param id ID del servicio
     * @return servicio con la disponibilidad actualizada
     */
    CatalogDTO toggleAvailability(Long id);

    /**
     * Elimina permanentemente un servicio del catálogo.
     *
     * @param id ID del servicio a eliminar
     */
    void deleteCatalog(Long id);
}
