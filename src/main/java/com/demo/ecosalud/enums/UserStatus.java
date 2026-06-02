package com.demo.ecosalud.enums;

/**
 * Estado de la cuenta de un usuario en el sistema.
 */
public enum UserStatus {
    /** Cuenta activa con acceso completo. */
    ACTIVO,
    /** Cuenta desactivada temporalmente. */
    INACTIVO,
    /** Cuenta inhabilitada (suspensión definitiva). */
    INABILITADO,
    /** Paciente que ha completado su ciclo de atención. */
    ATENDIDO
}
