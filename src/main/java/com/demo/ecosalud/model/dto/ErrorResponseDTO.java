package com.demo.ecosalud.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Respuesta de error estructurada que retorna el GlobalExceptionHandler.
 */
@Data
@AllArgsConstructor
public class ErrorResponseDTO {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
