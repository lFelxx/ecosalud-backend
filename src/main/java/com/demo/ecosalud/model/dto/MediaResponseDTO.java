package com.demo.ecosalud.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para archivos de media subidos al servidor.
 *
 * <p>El campo {@code url} es la URL pública con la que el frontend
 * puede renderizar la imagen directamente en {@code <img src="...">}.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponseDTO {

    /** Nombre de archivo generado en servidor (UUID + extensión). Se usa como ID. */
    private String id;

    /** Nombre original del archivo tal como lo subió el usuario. */
    private String name;

    /** URL pública para acceder al archivo: {@code /api/media/files/{tenant}/{id}}. */
    private String url;

    /** Tamaño del archivo en bytes. */
    private long size;

    /** MIME type del archivo (ej. {@code image/jpeg}). */
    private String mimeType;

    /** Timestamp ISO-8601 de cuándo se subió. */
    private String uploadedAt;
}
