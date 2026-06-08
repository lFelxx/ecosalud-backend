package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.MediaResponseDTO;
import com.demo.ecosalud.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

/**
 * Controlador REST para gestión de archivos de media.
 *
 * <p>Rutas:</p>
 * <ul>
 *   <li>{@code POST /api/media/upload}          — sube un archivo (requiere JWT)</li>
 *   <li>{@code GET  /api/media}                 — lista archivos del tenant (requiere JWT)</li>
 *   <li>{@code DELETE /api/media/{filename}}    — elimina archivo (requiere JWT)</li>
 *   <li>{@code GET  /api/media/files/{t}/{f}}   — sirve el archivo (público, sin JWT)</li>
 * </ul>
 *
 * <p>Los archivos se almacenan en disco: {@code uploads/{tenantSchema}/{uuid}.{ext}}</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    // ── Upload ────────────────────────────────────────────────────────────────

    /**
     * Sube un archivo de imagen al servidor.
     *
     * @param file    archivo recibido vía {@code multipart/form-data}
     * @param request para construir la URL base del servidor
     * @return 201 Created con el DTO del archivo subido
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponseDTO upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return mediaService.upload(file, baseUrl);
    }

    // ── List ─────────────────────────────────────────────────────────────────

    /**
     * Lista todos los archivos del tenant activo (ordenados por fecha desc).
     */
    @GetMapping
    public List<MediaResponseDTO> list(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return mediaService.list(baseUrl);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Elimina un archivo del tenant activo.
     *
     * @param filename nombre del archivo en servidor (UUID + extensión)
     */
    @DeleteMapping("/{filename}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String filename) {
        mediaService.delete(filename);
    }

    // ── Serve (público — sin JWT) ─────────────────────────────────────────────

    /**
     * Sirve un archivo de imagen directamente (solo en modo disco local).
     *
     * <p>Este endpoint es público: las imágenes se embeben en las páginas públicas
     * de cada clínica sin requerir autenticación.</p>
     *
     * <p><b>Nota — modo Cloudinary:</b> cuando el sistema usa Cloudinary, este endpoint
     * devuelve {@code 410 Gone} porque los archivos se sirven directamente desde la CDN
     * de Cloudinary. El frontend debe usar la URL absoluta devuelta por {@code /upload}
     * o {@code /list} en ese caso.</p>
     *
     * @param tenantSchema schema del tenant propietario (ej. {@code tenant_fisiosalud})
     * @param filename     nombre del archivo en disco
     */
    @GetMapping("/files/{tenantSchema}/{filename}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String tenantSchema,
            @PathVariable String filename) {
        try {
            Path filePath = mediaService.resolveFilePath(tenantSchema, filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Detectar content-type por extensión
            String contentType = detectContentType(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    // Cache 1 hora en cliente
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);

        } catch (UnsupportedOperationException e) {
            // Modo Cloudinary activo: los archivos se sirven desde CDN, no desde el backend.
            log.debug("[Media] serveFile llamado en modo Cloudinary — usa la URL de CDN");
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(null);
        } catch (MalformedURLException | IllegalArgumentException e) {
            log.warn("[Media] Intento de acceso inválido: {}/{} — {}", tenantSchema, filename, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream";
    }
}
