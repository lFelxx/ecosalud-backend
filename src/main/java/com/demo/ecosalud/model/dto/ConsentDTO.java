package com.demo.ecosalud.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para consentimientos informados digitales.
 *
 * <p>Entrada (POST): {@code serviceId}, {@code digitalSignature}.
 * El backend genera automáticamente {@code consentText}, {@code ipAddress},
 * {@code signedAt} y fuerza {@code patientId} del JWT.</p>
 *
 * <p>Salida: todos los campos incluyendo {@code patientName} y {@code serviceName}
 * resueltos por el servicio.</p>
 */
@Data
public class ConsentDTO {

    private Long id;

    // Entrada + salida
    private Long patientId;
    private Long serviceId;
    private String digitalSignature;

    // Solo salida — resueltos por el backend
    private String patientName;
    private String patientEmail;
    private String serviceName;
    private String consentText;
    private String ipAddress;
    private LocalDateTime signedAt;
    private LocalDateTime createdAt;
}
