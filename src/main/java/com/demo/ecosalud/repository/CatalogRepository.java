package com.demo.ecosalud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.model.entities.Catalog;

/**
 * Repositorio JPA para la entidad {@link Catalog}.
 */
public interface CatalogRepository extends JpaRepository<Catalog, Long> {

    /** Verifica si existe un servicio con el nombre dado (para evitar duplicados). */
    Boolean existsByName(String name);

    /** Retorna todos los servicios según su disponibilidad. */
    List<Catalog> findByAvailability(Boolean availability);

    /** Retorna todos los servicios de una categoría específica. */
    List<Catalog> findByCategoryIgnoreCase(String category);

    /** Retorna los servicios activos de una categoría específica. */
    List<Catalog> findByCategoryIgnoreCaseAndAvailability(String category, Boolean availability);
}
