package com.demo.ecosalud.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** DTO para publicaciones / blog de la clínica. */
@Data
public class PostDTO {
    private Long id;
    private String title;

    /** Slug URL-friendly — generado automáticamente del título si no se provee. */
    private String slug;

    private String excerpt;
    private String content;
    private String imageUrl;

    /** Estado: draft | published. */
    private String status;

    /** Tags separados por coma (ej. "salud,bienestar"). */
    private String tags;

    private Long authorId;
    private String authorName;

    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
