package com.demo.ecosalud.model.dto;

import lombok.Data;

/**
 * DTO para el perfil del especialista de la clínica.
 *
 * <p>El campo {@code name} se resuelve desde {@code tenant.users} usando
 * el {@code userId} vinculado. En PUT también actualiza el nombre del usuario.</p>
 */
@Data
public class SpecialistDTO {
    private Long id;
    private Long userId;

    /** Nombre completo — resuelto desde {@code tenant.users.name}. */
    private String name;

    private String specialty;

    /**
     * Matrícula profesional.
     * Puede contener dos credenciales separadas por '|'
     * (ej. "TP-12345|Esp. Corpas").
     */
    private String registrationNumber;

    private String bio;
    private String photoUrl;
    private String university;
    private String badge;
    private boolean active;
}
