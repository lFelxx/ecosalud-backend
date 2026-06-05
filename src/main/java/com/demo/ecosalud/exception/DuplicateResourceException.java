package com.demo.ecosalud.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso con datos ya existentes
 * (email, nombre, nombre de servicio). El {@link GlobalExceptionHandler} la convierte en HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
