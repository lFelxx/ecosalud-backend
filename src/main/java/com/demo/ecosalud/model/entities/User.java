package com.demo.ecosalud.model.entities;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa un usuario del sistema.
 * Mapea a la tabla {@code users} en PostgreSQL.
 */
@Entity
@Data
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_email", columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /** Email único: usado también como credencial de acceso. */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Contraseña almacenada como hash BCrypt (nunca en texto plano). */
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private RolUser role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;
}
