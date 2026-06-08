package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Publicación del blog de la plataforma Ecosalud Market.
 *
 * <p>Vive en el schema {@code public} (sin anotación de schema →
 * Hibernate usa el schema activo, que para el super-admin es siempre "public").
 * Creada y gestionada por el super-admin. Visible públicamente en
 * {@code GET /api/public/blog}.</p>
 *
 * <p><strong>Categorías:</strong>
 * <ul>
 *   <li>NOVEDAD  — nueva funcionalidad o mejora</li>
 *   <li>OFERTA   — promoción o descuento</li>
 *   <li>TUTORIAL — guía de uso de la plataforma</li>
 *   <li>GENERAL  — comunicado general</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "platform_posts", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** URL de imagen de portada (Cloudinary u otro CDN). Puede ser null. */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * Tags separados por coma. Ej: "fhir,novedades,SISPRO".
     * El frontend los convierte a array al parsear.
     */
    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * Categoría de la publicación.
     * Valores: NOVEDAD | OFERTA | TUTORIAL | GENERAL
     */
    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "GENERAL";

    /**
     * Estado de publicación.
     * Valores: DRAFT | PUBLISHED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    /** Nombre del autor (habitualmente "Equipo Ecosalud"). */
    @Column(name = "author_name", length = 255)
    @Builder.Default
    private String authorName = "Equipo Ecosalud";

    /** Momento en que el post fue publicado (null si DRAFT). */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
