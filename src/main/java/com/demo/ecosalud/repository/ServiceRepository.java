package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    /** Lista solo los servicios activos, ordenados por nombre. */
    List<Service> findByActiveTrueOrderByNameAsc();

    boolean existsByName(String name);
}
