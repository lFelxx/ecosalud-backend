package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.MediaResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Servicio de gestión de archivos de media por tenant.
 *
 * <p>Cada tenant tiene su propio subdirectorio en el servidor:
 * {@code uploads/{tenantSchema}/{uuid}.{ext}}</p>
 *
 * <p>Las URLs públicas siguen el patrón:
 * {@code /api/media/files/{tenantSchema}/{filename}}</p>
 */
public interface MediaService {

    /**
     * Sube un archivo y lo guarda en el directorio del tenant activo.
     *
     * @param file      archivo recibido vía multipart/form-data
     * @param baseUrl   URL base del servidor (ej. {@code http://localhost:8080})
     * @return DTO con la URL pública del archivo
     */
    MediaResponseDTO upload(MultipartFile file, String baseUrl);

    /**
     * Lista todos los archivos del tenant activo.
     *
     * @param baseUrl URL base del servidor
     * @return lista de DTOs con metadatos de cada archivo
     */
    List<MediaResponseDTO> list(String baseUrl);

    /**
     * Elimina un archivo del tenant activo.
     *
     * @param filename nombre de archivo generado por el servidor (UUID + extensión)
     */
    void delete(String filename);

    /**
     * Resuelve la ruta absoluta de un archivo para servirlo como recurso.
     *
     * @param tenantSchema schema del tenant propietario del archivo
     * @param filename     nombre de archivo en disco
     * @return ruta absoluta al archivo
     */
    java.nio.file.Path resolveFilePath(String tenantSchema, String filename);
}
