package com.demo.ecosalud.enums;

/**
 * Tipo de cuenta de un tenant dentro de la plataforma.
 */
public enum AccountType {
    /** Clínica de pago estándar. */
    REGULAR,
    /** Clínica fundadora — exenta de pago, acceso CLINIC ilimitado. */
    FOUNDER,
    /** Cuenta de demostración — datos de prueba, no facturable. */
    DEMO
}
