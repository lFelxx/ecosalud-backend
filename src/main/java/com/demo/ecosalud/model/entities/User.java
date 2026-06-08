package com.demo.ecosalud.model.entities;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Representa la cuenta de un usuario en el sistema.
 * Puede tener los roles: USER (paciente), THERAPIST o ADMIN.
 */
@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private RolUser role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * Número de teléfono del usuario (opcional).
     * Formato internacional recomendado: {@code +57XXXXXXXXXX}.
     * Usado por el job de recordatorios para enviar SMS/WhatsApp vía Twilio.
     */
    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
