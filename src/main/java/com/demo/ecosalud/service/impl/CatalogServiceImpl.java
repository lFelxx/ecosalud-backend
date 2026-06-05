package com.demo.ecosalud.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.demo.ecosalud.exception.DuplicateResourceException;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.mapper.CatalogMapper;
import com.demo.ecosalud.model.dto.CatalogDTO;
import com.demo.ecosalud.model.entities.Catalog;
import com.demo.ecosalud.repository.CatalogRepository;
import com.demo.ecosalud.service.CatalogService;

/**
 * Implementación del servicio de gestión del catálogo de servicios.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class CatalogServiceImpl implements CatalogService {

    private final CatalogRepository catalogRepository;

    /**
     * Crea un nuevo servicio en el catálogo.
     * Valida que el nombre no esté duplicado en el sistema.
     *
     * @param catalogDTO datos del servicio enviados desde el frontend
     * @return servicio creado con ID asignado
     */
    @Override
    public CatalogDTO createCatalog(CatalogDTO catalogDTO) {
        if (catalogRepository.existsByName(catalogDTO.getName())) {
            throw new DuplicateResourceException("Ya existe un servicio con el nombre: " + catalogDTO.getName());
        }

        Catalog catalog = CatalogMapper.toEntity(catalogDTO);
        Catalog saved = catalogRepository.save(catalog);
        return CatalogMapper.toDTO(saved);
    }

    /**
     * Obtiene un servicio del catálogo por su ID.
     */
    @Override
    public CatalogDTO getCatalogById(Long id) {
        return catalogRepository.findById(id)
                .map(CatalogMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));
    }

    /**
     * Retorna todos los servicios o los de una categoría si se proporciona (uso administrativo).
     */
    @Override
    public List<CatalogDTO> getCatalogs(String category) {
        List<Catalog> results = (category == null || category.isBlank())
                ? catalogRepository.findAll()
                : catalogRepository.findByCategoryIgnoreCase(category);
        return results.stream().map(CatalogMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Retorna los servicios activos, filtrados por categoría si se proporciona.
     */
    @Override
    public List<CatalogDTO> getAvailableCatalogs(String category) {
        List<Catalog> results = (category == null || category.isBlank())
                ? catalogRepository.findByAvailability(true)
                : catalogRepository.findByCategoryIgnoreCaseAndAvailability(category, true);
        return results.stream().map(CatalogMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Actualiza los datos de un servicio. Verifica que el nuevo nombre
     * no esté en uso por otro servicio diferente.
     *
     * @param id         ID del servicio a actualizar
     * @param catalogDTO nuevos datos del servicio
     * @return servicio actualizado
     */
    @Override
    public CatalogDTO updateCatalog(Long id, CatalogDTO catalogDTO) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));

        boolean nameChanged = !catalog.getName().equalsIgnoreCase(catalogDTO.getName());
        if (nameChanged && catalogRepository.existsByName(catalogDTO.getName())) {
            throw new DuplicateResourceException("Ya existe un servicio con el nombre: " + catalogDTO.getName());
        }

        catalog.setName(catalogDTO.getName());
        catalog.setDescription(catalogDTO.getDescription());
        catalog.setPrice(catalogDTO.getPrice());
        catalog.setDuration(catalogDTO.getDuration());

        if (catalogDTO.getAvailability() != null) {
            catalog.setAvailability(catalogDTO.getAvailability());
        }

        if (catalogDTO.getCategory() != null) {
            catalog.setCategory(catalogDTO.getCategory());
        }

        Catalog saved = catalogRepository.save(catalog);
        return CatalogMapper.toDTO(saved);
    }

    /**
     * Invierte la disponibilidad del servicio: activo → inactivo o viceversa.
     */
    @Override
    public CatalogDTO toggleAvailability(Long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));

        catalog.setAvailability(!catalog.getAvailability());
        Catalog saved = catalogRepository.save(catalog);
        return CatalogMapper.toDTO(saved);
    }

    /**
     * Elimina permanentemente un servicio del catálogo.
     */
    @Override
    public void deleteCatalog(Long id) {
        if (!catalogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Servicio no encontrado con id: " + id);
        }
        catalogRepository.deleteById(id);
    }
}
