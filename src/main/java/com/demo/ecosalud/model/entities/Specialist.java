package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA para el perfil clínico extendido del especialista de la clínica.
 * Mapea a la tabla {@code specialists} en el schema del tenant activo.
 *
 * <p>Un specialist está vinculado a un {@code users.id} ({@code user_id}).
 * Cada tenant tiene generalmente un solo especialista principal.</p>
 */
@Entity
@Data
@Table(name = "specialists")
public class Specialist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** FK al usuario propietario del perfil en {@code tenant.users}. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "specialty")
    private String specialty;

    /**
     * Número de matrícula profesional.
     * Puede contener dos credenciales separadas por '|' (ej. "TP-12345|EspCorp").
     */
    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @Column(name = "university")
    private String university;

    @Column(name = "badge", length = 100)
    private String badge;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
