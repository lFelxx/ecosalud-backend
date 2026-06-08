package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para publicaciones de la plataforma.
 *
 * <p>Se usa tanto en el endpoint público ({@code GET /api/public/blog})
 * como en los endpoints de gestión del super-admin ({@code /api/platform/blog}).</p>
 */
@Data
@Builder
public class PlatformPostDTO {

    private Long   id;
    private String title;
    private String excerpt;

    /** Incluido solo en detalle de post (GET /{id}). En listados puede ser null. */
    private String content;

    private String imageUrl;

    /** Lista de tags parseada desde el campo CSV. */
    private List<String> tags;

    /** NOVEDAD | OFERTA | TUTORIAL | GENERAL */
    private String category;

    /** DRAFT | PUBLISHED */
    private String status;

    private String authorName;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Request fields (solo escritura) ─────────────────────────────────────────

    /** Tags como string CSV — usado en POST/PUT request body. */
    private String tagsRaw;
}
