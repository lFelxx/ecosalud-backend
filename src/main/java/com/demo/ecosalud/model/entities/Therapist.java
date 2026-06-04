package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Representa el perfil profesional de un terapeuta en el sistema.
 * Extiende la información básica del usuario con datos propios de su práctica clínica.
 * Relación: un Usuario con rol THERAPIST puede tener exactamente un perfil Therapist.
 */
@Entity
@Data
@Table(name = "therapists")
public class Therapist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Cuenta de usuario asociada. Un terapeuta siempre tiene un User con rol THERAPIST. */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Especialidad clínica del terapeuta (ej: Psicología, Fisioterapia). Puede completarse después del registro. */
    @Column(name = "specialty")
    private String specialty;

    /** Descripción profesional visible para los pacientes al elegir especialista. */
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    /** Años de experiencia profesional. */
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    /** Indica si el terapeuta está aceptando nuevas citas actualmente. */
    @Column(name = "available", nullable = false)
    private Boolean available = true;
}
