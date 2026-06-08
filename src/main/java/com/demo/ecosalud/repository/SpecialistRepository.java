package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpecialistRepository extends JpaRepository<Specialist, Long> {

    /** Devuelve el primer especialista activo del tenant (generalmente hay solo uno). */
    Optional<Specialist> findFirstByActiveTrueOrderByIdAsc();

    /** Busca el especialista vinculado a un usuario. */
    Optional<Specialist> findByUserId(Long userId);
}
