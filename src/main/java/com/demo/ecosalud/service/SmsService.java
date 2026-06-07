package com.demo.ecosalud.service;

/**
 * Contrato del servicio de mensajes de texto (SMS) y WhatsApp.
 *
 * <p>Implementado con la API REST de <strong>Twilio</strong>.
 * Si las credenciales no están configuradas, los mensajes se loguean
 * en consola sin fallar la aplicación.</p>
 *
 * <p>Configuración requerida (variables de entorno):</p>
 * <pre>
 *   TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   TWILIO_AUTH_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
 *   TWILIO_FROM_NUMBER=+15005550006          # número Twilio (SMS)
 *   TWILIO_WHATSAPP_FROM=whatsapp:+14155238886 # sender WhatsApp Sandbox
 * </pre>
 *
 * <p>Para activar WhatsApp Sandbox de Twilio:</p>
 * <ol>
 *   <li>Ir a <a href="https://console.twilio.com/us1/develop/sms/try-it-out/whatsapp-learn">
 *       Twilio WhatsApp Sandbox</a></li>
 *   <li>Enviar el código de activación desde el número del paciente</li>
 *   <li>El sandbox ya puede enviar mensajes a ese número</li>
 * </ol>
 */
public interface SmsService {

    /**
     * Indica si el servicio SMS está habilitado (credenciales configuradas).
     *
     * @return {@code true} si {@code TWILIO_ACCOUNT_SID} y {@code TWILIO_AUTH_TOKEN}
     *         están configurados con valores no vacíos.
     */
    boolean isSmsEnabled();

    /**
     * Indica si el servicio WhatsApp está habilitado.
     *
     * @return {@code true} si además de SMS, {@code TWILIO_WHATSAPP_FROM} está configurado.
     */
    boolean isWhatsAppEnabled();

    /**
     * Envía un SMS al número de teléfono especificado.
     *
     * <p>Si el servicio no está habilitado, el mensaje se loguea en consola.</p>
     *
     * @param toPhone número destino en formato internacional (p.ej. {@code +573001234567})
     * @param body    contenido del mensaje (máx. 1600 caracteres para SMS concatenados)
     */
    void sendSms(String toPhone, String body);

    /**
     * Envía un mensaje de WhatsApp al número especificado.
     *
     * <p>Requiere que el destinatario haya activado el sandbox de Twilio
     * o que se use el número oficial de WhatsApp Business.</p>
     *
     * @param toPhone número destino en formato internacional (p.ej. {@code +573001234567})
     * @param body    contenido del mensaje WhatsApp
     */
    void sendWhatsApp(String toPhone, String body);

    /**
     * Envía recordatorio de cita: intenta WhatsApp primero, cae a SMS si no disponible.
     *
     * @param toPhone     número del paciente (puede ser null — se ignora si lo es)
     * @param patientName nombre del paciente
     * @param service     nombre de la terapia
     * @param dateTime    fecha y hora de la cita (texto formateado)
     * @param clinicName  nombre de la clínica
     */
    void sendAppointmentReminder(String toPhone, String patientName,
                                  String service, String dateTime, String clinicName);
}
