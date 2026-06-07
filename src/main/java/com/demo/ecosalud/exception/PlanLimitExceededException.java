package com.demo.ecosalud.exception;

import lombok.Getter;

/**
 * Se lanza cuando un tenant intenta crear un recurso que excede el límite
 * de su plan de suscripción actual.
 *
 * <p>Capturada por {@link GlobalExceptionHandler} y convertida en HTTP 402.</p>
 */
@Getter
public class PlanLimitExceededException extends RuntimeException {

    public enum LimitType { APPOINTMENTS, PATIENTS, TRIAL_EXPIRED }

    private final LimitType limitType;
    private final int       current;
    private final int       max;
    private final String    plan;

    public PlanLimitExceededException(LimitType limitType, int current, int max, String plan) {
        super(buildMessage(limitType, current, max, plan));
        this.limitType = limitType;
        this.current   = current;
        this.max       = max;
        this.plan      = plan;
    }

    private static String buildMessage(LimitType type, int current, int max, String plan) {
        return switch (type) {
            case APPOINTMENTS -> String.format(
                "Tu plan %s permite máximo %d citas por mes y ya tienes %d este mes. " +
                "Actualiza tu plan para continuar.", plan, max, current);
            case PATIENTS -> String.format(
                "Tu plan %s permite máximo %d pacientes registrados y ya tienes %d. " +
                "Actualiza tu plan para continuar.", plan, max, current);
            case TRIAL_EXPIRED ->
                "Tu período de prueba de 14 días ha vencido. " +
                "Activa una suscripción para continuar creando citas y registrando pacientes. " +
                "Tus datos existentes están seguros.";
        };
    }
}
