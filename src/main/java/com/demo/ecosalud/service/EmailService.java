package com.demo.ecosalud.service;

/**
 * Contrato del servicio de correo electrónico transaccional.
 *
 * <p>Implementado con la API de Resend ({@code https://resend.com}).
 * Plan gratuito: 3,000 emails/mes · 100 emails/día — suficiente para el inicio.</p>
 *
 * <p>Para activar, define la variable de entorno {@code RESEND_API_KEY}
 * con la clave obtenida en {@code https://resend.com/api-keys}.</p>
 *
 * <p>Si no hay API key configurada, los emails se loguean en consola
 * (modo desarrollo) sin fallar la aplicación.</p>
 */
public interface EmailService {

    /**
     * Envía un email de confirmación de cita al paciente.
     *
     * @param toEmail      correo del paciente
     * @param patientName  nombre del paciente
     * @param serviceName  nombre de la terapia agendada
     * @param dateTime     fecha y hora de la cita (texto formateado)
     * @param specialistName nombre del especialista
     * @param clinicName   nombre de la clínica
     */
    void sendAppointmentConfirmation(
            String toEmail,
            String patientName,
            String serviceName,
            String dateTime,
            String specialistName,
            String clinicName
    );

    /**
     * Envía un recordatorio de cita 24 horas antes.
     *
     * @param toEmail     correo del paciente
     * @param patientName nombre del paciente
     * @param serviceName nombre de la terapia
     * @param dateTime    fecha y hora de la cita
     * @param clinicName  nombre de la clínica
     */
    void sendAppointmentReminder(
            String toEmail,
            String patientName,
            String serviceName,
            String dateTime,
            String clinicName
    );

    /**
     * Envía las credenciales de acceso a un nuevo paciente registrado.
     *
     * @param toEmail     correo del nuevo paciente
     * @param patientName nombre del paciente
     * @param password    contraseña temporal generada
     * @param clinicName  nombre de la clínica donde se registró
     * @param loginUrl    URL de login de la clínica
     */
    void sendWelcomeCredentials(
            String toEmail,
            String patientName,
            String password,
            String clinicName,
            String loginUrl
    );

    /**
     * Envía notificación de cancelación de cita.
     *
     * @param toEmail          correo del paciente
     * @param patientName      nombre del paciente
     * @param serviceName      nombre de la terapia cancelada
     * @param dateTime         fecha y hora original de la cita
     * @param cancellationNote motivo de cancelación (opcional)
     */
    void sendAppointmentCancellation(
            String toEmail,
            String patientName,
            String serviceName,
            String dateTime,
            String cancellationNote
    );

    /**
     * Avisa al propietario de la clínica que su trial vence en {@code daysLeft} días.
     *
     * @param toEmail    correo del propietario
     * @param ownerName  nombre del propietario
     * @param clinicName nombre de la clínica
     * @param daysLeft   días restantes del período de prueba
     * @param billingUrl URL directa a la página de suscripción de la clínica
     */
    void sendTrialExpiring(
            String toEmail,
            String ownerName,
            String clinicName,
            int    daysLeft,
            String billingUrl
    );

    /**
     * Notifica al propietario que el período de prueba ha vencido y su cuenta
     * está en estado {@code OVERDUE} — la creación de nuevos recursos está bloqueada.
     *
     * @param toEmail    correo del propietario
     * @param ownerName  nombre del propietario
     * @param clinicName nombre de la clínica
     * @param billingUrl URL directa a la página de suscripción de la clínica
     */
    void sendTrialExpired(
            String toEmail,
            String ownerName,
            String clinicName,
            String billingUrl
    );
}
