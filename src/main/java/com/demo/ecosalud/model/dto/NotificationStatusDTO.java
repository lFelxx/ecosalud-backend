package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO de respuesta del panel de notificaciones del admin.
 *
 * <p>Informa el estado de los canales de notificación configurados
 * y lista las citas próximas que recibirán recordatorio automático.</p>
 */
@Data
@Builder
public class NotificationStatusDTO {

    // ── Estado de canales ──────────────────────────────────────────────────────

    /** {@code true} si Resend está configurado y puede enviar emails. */
    private boolean emailEnabled;

    /** {@code true} si Twilio SMS está configurado. */
    private boolean smsEnabled;

    /** {@code true} si Twilio WhatsApp está configurado. */
    private boolean whatsAppEnabled;

    /** Expresión cron del job de recordatorios (p.ej. {@code "0 0 9 * * *"}). */
    private String reminderCron;

    /** Descripción legible del cron (p.ej. "Todos los días a las 9:00 AM"). */
    private String reminderCronDescription;

    // ── Próximos recordatorios ─────────────────────────────────────────────────

    /** Número de citas mañana que recibirán recordatorio. */
    private int tomorrowCount;

    /** Número de citas en los próximos 7 días. */
    private int next7DaysCount;

    /** Lista detallada de las próximas citas con recordatorio programado. */
    private List<UpcomingReminder> upcoming;

    // ── Sub-DTO ────────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class UpcomingReminder {
        private Long   appointmentId;
        private String patientName;
        private String patientEmail;
        private boolean patientHasPhone;
        private String date;           // "2026-06-08"
        private String time;           // "09:00"
        private String status;
        private boolean emailWillBeSent;
        private boolean smsWillBeSent;
    }
}
