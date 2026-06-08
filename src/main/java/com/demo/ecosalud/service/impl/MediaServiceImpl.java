package com.demo.ecosalud.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.demo.ecosalud.model.dto.MediaResponseDTO;
import com.demo.ecosalud.multitenancy.TenantContext;
import com.demo.ecosalud.service.MediaService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Implementación de {@link MediaService} con soporte dual de almacenamiento.
 *
 * <h3>Modo Cloudinary (producción)</h3>
 * <p>Activo cuando la variable de entorno {@code CLOUDINARY_URL} está configurada.
 * Los archivos se suben al bucket de Cloudinary y se sirven directamente desde
 * la CDN de Cloudinary. Las URLs devueltas son absolutas ({@code https://res.cloudinary.com/...}).
 * Los archivos persisten entre redeploys en Railway/Render.</p>
 *
 * <pre>
 *   Estructura en Cloudinary:
 *     ecosalud/{tenantSchema}/{uuid}  (sin extensión — Cloudinary la gestiona)
 * </pre>
 *
 * <h3>Modo disco local (desarrollo)</h3>
 * <p>Activo cuando {@code CLOUDINARY_URL} no está configurado.
 * Los archivos se guardan en {@code uploads/{tenantSchema}/{uuid}.{ext}}.
 * Las URLs son relativas al servidor backend.</p>
 *
 * <pre>
 *   uploads/
 *     tenant_dra_angelica_camacho/
 *       3f2a1b4c-uuid.png
 *     tenant_fisiosalud/
 *       8d9e2f1a-uuid.jpg
 * </pre>
 */
@Slf4j
@Service
public class MediaServiceImpl implements MediaService {

    // ── Configuración ─────────────────────────────────────────────────────────

    /**
     * URL de Cloudinary en formato {@code cloudinary://api_key:api_secret@cloud_name}.
     * Si está vacía el sistema cae en modo disco local.
     */
    @Value("${CLOUDINARY_URL:}")
    private String cloudinaryUrl;

    /** Directorio raíz de uploads para modo disco local. */
    @Value("${media.upload-dir:uploads}")
    private String uploadDirBase;

    /** Instancia de Cloudinary — null si modo disco local. */
    private Cloudinary cloudinary;

    /** Extensiones de imagen permitidas. */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "svg");

    /** Tamaño máximo por archivo: 10 MB. */
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    // ── Inicialización ────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            cloudinary = new Cloudinary(cloudinaryUrl);
            log.info("[Media] Modo CLOUDINARY activado ({})",
                    cloudinaryUrl.replaceAll(":([^@]+)@", ":***@")); // oculta secret en log
        } else {
            log.info("[Media] Modo DISCO LOCAL activado — dir: {}", uploadDirBase);
        }
    }

    private boolean isCloudinaryEnabled() {
        return cloudinary != null;
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    @Override
    public MediaResponseDTO upload(MultipartFile file, String baseUrl) {
        validateFile(file);
        return isCloudinaryEnabled()
                ? uploadToCloudinary(file)
                : uploadToDisk(file, baseUrl);
    }

    private MediaResponseDTO uploadToCloudinary(MultipartFile file) {
        String tenant = TenantContext.get();
        String uuid   = UUID.randomUUID().toString();
        String folder = "ecosalud/" + tenant;

        try {
            byte[] bytes = file.getBytes();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                            "folder",        folder,
                            "public_id",     uuid,
                            "resource_type", "image"
                    )
            );

            String secureUrl  = (String) result.get("secure_url");
            String format     = (String) result.get("format");
            long   sizeBytes  = result.get("bytes") != null ? ((Number) result.get("bytes")).longValue() : 0L;
            String createdAt  = (String) result.get("created_at");

            log.info("[Media/Cloudinary] Subido: {}/{}", tenant, uuid);

            return MediaResponseDTO.builder()
                    .id(uuid)
                    .name(file.getOriginalFilename())
                    .url(secureUrl)
                    .size(sizeBytes)
                    .mimeType(mimeTypeFromFormat(format, file.getOriginalFilename()))
                    .uploadedAt(createdAt != null ? createdAt : Instant.now().toString())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Error al subir archivo a Cloudinary: " + e.getMessage(), e);
        }
    }

    private MediaResponseDTO uploadToDisk(MultipartFile file, String baseUrl) {
        String tenant    = TenantContext.get();
        Path   tenantDir = tenantDirectory(tenant);

        try {
            Files.createDirectories(tenantDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio del tenant: " + tenantDir, e);
        }

        String ext      = extension(Objects.requireNonNull(file.getOriginalFilename()));
        String filename = UUID.randomUUID() + "." + ext;
        Path   dest     = tenantDir.resolve(filename);

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + filename, e);
        }

        log.info("[Media/Disco] Subido: {}/{}", tenant, filename);

        return MediaResponseDTO.builder()
                .id(filename)
                .name(file.getOriginalFilename())
                .url(buildLocalUrl(baseUrl, tenant, filename))
                .size(file.getSize())
                .mimeType(file.getContentType())
                .uploadedAt(Instant.now().toString())
                .build();
    }

    // ── List ─────────────────────────────────────────────────────────────────

    @Override
    public List<MediaResponseDTO> list(String baseUrl) {
        return isCloudinaryEnabled()
                ? listFromCloudinary()
                : listFromDisk(baseUrl);
    }

    @SuppressWarnings("unchecked")
    private List<MediaResponseDTO> listFromCloudinary() {
        String tenant = TenantContext.get();
        String prefix = "ecosalud/" + tenant;

        try {
            Map<String, Object> result = (Map<String, Object>) cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type",        "upload",
                            "prefix",      prefix,
                            "max_results", 500
                    )
            );

            List<Map<String, Object>> resources =
                    (List<Map<String, Object>>) result.get("resources");

            if (resources == null) return Collections.emptyList();

            return resources.stream()
                    .map(r -> {
                        // public_id = "ecosalud/tenant_xxx/uuid-value"
                        String publicId = (String) r.get("public_id");
                        String uuid     = publicId != null && publicId.contains("/")
                                ? publicId.substring(publicId.lastIndexOf('/') + 1)
                                : publicId;
                        String format   = (String) r.get("format");
                        long   size     = r.get("bytes") != null
                                ? ((Number) r.get("bytes")).longValue() : 0L;

                        return MediaResponseDTO.builder()
                                .id(uuid)
                                .name(uuid + (format != null ? "." + format : ""))
                                .url((String) r.get("secure_url"))
                                .size(size)
                                .mimeType(mimeTypeFromFormat(format, null))
                                .uploadedAt((String) r.get("created_at"))
                                .build();
                    })
                    .toList();

        } catch (Exception e) {
            log.error("[Media/Cloudinary] Error al listar archivos del tenant '{}': {}",
                    tenant, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<MediaResponseDTO> listFromDisk(String baseUrl) {
        String tenant    = TenantContext.get();
        Path   tenantDir = tenantDirectory(tenant);

        if (!Files.exists(tenantDir)) return Collections.emptyList();

        try (Stream<Path> files = Files.list(tenantDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparingLong((Path p) -> {
                        try { return Files.getLastModifiedTime(p).toMillis(); }
                        catch (IOException e) { return 0L; }
                    }).reversed())
                    .map(p -> {
                        try {
                            String fname = p.getFileName().toString();
                            return MediaResponseDTO.builder()
                                    .id(fname)
                                    .name(fname)
                                    .url(buildLocalUrl(baseUrl, tenant, fname))
                                    .size(Files.size(p))
                                    .mimeType(mimeTypeFromExt(extension(fname)))
                                    .uploadedAt(Files.getLastModifiedTime(p).toInstant().toString())
                                    .build();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException e) {
            log.error("[Media/Disco] Error al listar archivos del tenant '{}': {}",
                    tenant, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(String id) {
        if (isCloudinaryEnabled()) {
            deleteFromCloudinary(id);
        } else {
            deleteFromDisk(id);
        }
    }

    private void deleteFromCloudinary(String uuid) {
        // Validar que no sea path traversal
        if (uuid.contains("..") || uuid.contains("/") || uuid.contains("\\")) {
            throw new IllegalArgumentException("ID de recurso no válido: " + uuid);
        }

        String tenant   = TenantContext.get();
        String publicId = "ecosalud/" + tenant + "/" + uuid;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
            log.info("[Media/Cloudinary] Eliminado: {}", publicId);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar archivo de Cloudinary: " + e.getMessage(), e);
        }
    }

    private void deleteFromDisk(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Nombre de archivo no válido: " + filename);
        }

        String tenant = TenantContext.get();
        Path   file   = tenantDirectory(tenant).resolve(filename);

        try {
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) {
                log.info("[Media/Disco] Eliminado: {}/{}", tenant, filename);
            } else {
                log.warn("[Media/Disco] Archivo no encontrado: {}/{}", tenant, filename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo: " + filename, e);
        }
    }

    // ── Serve (sólo modo disco local) ─────────────────────────────────────────

    /**
     * Resuelve la ruta absoluta de un archivo para servirlo como recurso de disco.
     *
     * <p><b>Solo disponible en modo disco local.</b>
     * En modo Cloudinary los archivos se sirven directamente desde la CDN de Cloudinary
     * usando la URL devuelta por {@link #upload} o {@link #list};
     * llamar este método en ese contexto lanza {@link UnsupportedOperationException}.</p>
     *
     * @param tenantSchema schema del tenant propietario (ej. {@code tenant_fisiosalud})
     * @param filename     nombre de archivo en disco (ej. {@code uuid.jpg})
     * @return ruta absoluta al archivo en disco
     * @throws UnsupportedOperationException si se invoca en modo Cloudinary
     */
    @Override
    public Path resolveFilePath(String tenantSchema, String filename) {
        if (isCloudinaryEnabled()) {
            throw new UnsupportedOperationException(
                    "resolveFilePath no está disponible en modo Cloudinary. " +
                    "Usa la URL de CDN devuelta por upload() o list().");
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }
        if (tenantSchema.contains("..") || tenantSchema.contains("/")) {
            throw new IllegalArgumentException("Tenant no válido");
        }
        return tenantDirectory(tenantSchema).resolve(filename);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Path tenantDirectory(String tenantSchema) {
        return Path.of(uploadDirBase).toAbsolutePath().resolve(tenantSchema);
    }

    private String buildLocalUrl(String baseUrl, String tenantSchema, String filename) {
        return baseUrl + "/api/media/files/" + tenantSchema + "/" + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo de 10 MB.");
        }
        String ext = extension(Objects.requireNonNull(file.getOriginalFilename()));
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido: " + ext +
                    ". Permitidos: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1) : "";
    }

    private String mimeTypeFromExt(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"         -> "image/png";
            case "gif"         -> "image/gif";
            case "webp"        -> "image/webp";
            case "svg"         -> "image/svg+xml";
            default            -> "application/octet-stream";
        };
    }

    /**
     * Resuelve el MIME type a partir del formato de Cloudinary (sin extensión)
     * o del nombre de archivo original.
     */
    private String mimeTypeFromFormat(String format, String originalFilename) {
        if (format != null && !format.isBlank()) {
            return mimeTypeFromExt(format);
        }
        if (originalFilename != null) {
            return mimeTypeFromExt(extension(originalFilename));
        }
        return "application/octet-stream";
    }
}
