package com.demo.ecosalud.service;

/**
 * Servicio encargado de verificar si el tenant activo puede crear nuevos recursos
 * según los límites de su plan de suscripción.
 *
 * <p>Los controladores deben invocar el método correspondiente <em>antes</em>
 * de persistir el recurso. Si el límite está superado se lanza
 * {@link com.demo.ecosalud.exception.PlanLimitExceededException} (HTTP 402).</p>
 */
public interface PlanLimitsService {

    /**
     * Verifica que el tenant activo no haya alcanzado su cuota de citas
     * del mes calendario actual.
     *
     * @throws com.demo.ecosalud.exception.PlanLimitExceededException si se superó el límite
     */
    void checkAppointmentLimit();

    /**
     * Verifica que el tenant activo no haya alcanzado su cuota de pacientes
     * registrados (usuarios con rol USER).
     *
     * @throws com.demo.ecosalud.exception.PlanLimitExceededException si se superó el límite
     */
    void checkPatientLimit();
}
