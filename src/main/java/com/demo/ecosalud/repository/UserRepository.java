package com.demo.ecosalud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByName(String name);

    Boolean existsByStatus(UserStatus status);

    /**
     * Retorna todos los usuarios con un rol específico en el tenant activo.
     * Usado por {@code FhirMapperService} para exponer pacientes (rol USER) en FHIR R4.
     */
    List<User> findByRole(RolUser role);

    /**
     * Retorna un usuario por su ID y rol.
     * Usado por {@code FhirMapperService} para obtener un paciente específico en FHIR R4.
     */
    Optional<User> findByIdAndRole(Long id, RolUser role);

    /**
     * Cuenta usuarios con un rol específico en el tenant activo.
     * Usado por {@code PlanLimitsService} para verificar el límite de pacientes (rol USER).
     */
    long countByRole(RolUser role);
}
