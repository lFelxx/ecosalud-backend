package com.demo.ecosalud.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la base de datos.
 * El {@link GlobalExceptionHandler} la convierte en respuesta HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
