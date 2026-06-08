package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entidad JPA para las publicaciones / blog de la clínica.
 * Mapea a la tabla {@code posts} en el schema del tenant activo.
 *
 * <p>El campo {@code slug} es único por schema de tenant y se genera
 * automáticamente a partir del título al crear la publicación.</p>
 */
@Entity
@Data
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    /** Identificador URL-friendly único (generado del título). */
    @Column(name = "slug", nullable = false, unique = true, length = 500)
    private String slug;

    @Column(name = "excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    /**
     * Estado de publicación: draft | published.
     * Solo los posts con {@code published} son visibles públicamente.
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "draft";

    /** Tags separados por coma (ej. "salud,bienestar,terapia"). */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
