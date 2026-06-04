package com.demo.ecosalud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.model.entities.Therapist;

/**
 * Repositorio JPA para la entidad {@link Therapist}.
 */
public interface TherapistRepository extends JpaRepository<Therapist, Long> {

    /** Verifica si ya existe un perfil de terapeuta para el usuario dado. */
    Boolean existsByUserId(Long userId);

    /** Busca el perfil de terapeuta por el ID de su usuario asociado. */
    Optional<Therapist> findByUserId(Long userId);

    /** Retorna todos los terapeutas según su disponibilidad. */
    List<Therapist> findByAvailable(Boolean available);

    /** Retorna todos los terapeutas cuya especialidad contiene el texto dado (insensible a mayúsculas). */
    List<Therapist> findBySpecialtyContainingIgnoreCase(String specialty);

    /** Retorna los terapeutas disponibles cuya especialidad contiene el texto dado. */
    List<Therapist> findBySpecialtyContainingIgnoreCaseAndAvailable(String specialty, Boolean available);
}
