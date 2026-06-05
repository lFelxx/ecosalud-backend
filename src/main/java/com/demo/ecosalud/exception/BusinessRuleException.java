package com.demo.ecosalud.exception;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio
 * (terapeuta no disponible, conflicto de horario, cita ya cancelada, etc.).
 * El {@link GlobalExceptionHandler} la convierte en HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
