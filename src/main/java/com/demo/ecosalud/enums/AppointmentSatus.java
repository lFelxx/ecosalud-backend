package com.demo.ecosalud.enums;

/**
 * Estados posibles de una cita médica.
 *
 * <p>Nota: el nombre de clase tiene un typo histórico ("Satus" en lugar de "Status").
 * Se mantiene para no romper datos existentes en BD; renombrar requiere migración.</p>
 */
public enum AppointmentSatus {
    /** Cita registrada, pendiente de confirmación por el especialista. */
    PENDIENTE,
    /** Cita confirmada por el especialista. */
    CONFIRMADA,
    /** Cita cancelada por el paciente o el especialista. */
    CANCELADA,
    /** Cita completada exitosamente. */
    COMPLETADA
}
