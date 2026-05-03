package com.demo.ecosalud.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.model.entities.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    Boolean existsByName(String name);
    Boolean existsByUserId(Long userId);
    Boolean existsByAvailability(Boolean availability);
    
}
