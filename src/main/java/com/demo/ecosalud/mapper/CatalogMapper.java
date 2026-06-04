package com.demo.ecosalud.mapper;

import com.demo.ecosalud.model.dto.CatalogDTO;
import com.demo.ecosalud.model.entities.Catalog;

/**
 * Convierte entre la entidad {@link Catalog} y su {@link CatalogDTO}.
 */
public class CatalogMapper {

    /**
     * Convierte un DTO a entidad Catalog lista para persistir.
     * La disponibilidad por defecto es {@code true} cuando no se especifica.
     *
     * @param dto datos del servicio enviados desde el frontend
     * @return entidad Catalog sin ID (se asigna al guardar)
     */
    public static Catalog toEntity(CatalogDTO dto) {
        Catalog catalog = new Catalog();
        catalog.setName(dto.getName());
        catalog.setDescription(dto.getDescription());
        catalog.setPrice(dto.getPrice());
        catalog.setDuration(dto.getDuration());
        catalog.setAvailability(dto.getAvailability() != null ? dto.getAvailability() : true);
        catalog.setCategory(dto.getCategory());
        catalog.setBenefits(dto.getBenefits());
        return catalog;
    }

    /**
     * Convierte una entidad Catalog a DTO de respuesta.
     *
     * @param catalog entidad a convertir
     * @return DTO listo para serializar como respuesta JSON
     */
    public static CatalogDTO toDTO(Catalog catalog) {
        CatalogDTO dto = new CatalogDTO();
        dto.setId(catalog.getId());
        dto.setName(catalog.getName());
        dto.setDescription(catalog.getDescription());
        dto.setPrice(catalog.getPrice());
        dto.setDuration(catalog.getDuration());
        dto.setAvailability(catalog.getAvailability());
        dto.setCategory(catalog.getCategory());
        dto.setBenefits(catalog.getBenefits());
        return dto;
    }
}
