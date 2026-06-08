package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA para los servicios / terapias ofrecidos por la clínica.
 * Mapea a la tabla {@code services} en el schema del tenant activo.
 */
@Entity
@Data
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Beneficios del servicio (líneas separadas por '\n'). */
    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "price_cop", precision = 12, scale = 2)
    private BigDecimal priceCop;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
