package com.demo.ecosalud.scheduler;

import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.multitenancy.TenantContext;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.EmailService;
import com.demo.ecosalud.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Job programado que envía recordatorios automáticos de cita a los pacientes.
 *
 * <h3>Lógica</h3>
 * <ol>
 *   <li>Itera sobre todos los tenants activos con schema inicializado.</li>
 *   <li>Para cada tenant, establece el contexto de multi-tenancy.</li>
 *   <li>Busca citas de <strong>mañana</strong> en estado PENDIENTE o CONFIRMADA.</li>
 *   <li>Para cada cita: envía email al paciente y SMS/WhatsApp si tiene teléfono.</li>
 * </ol>
 *
 * <h3>Configuración</h3>
 * <pre>
 *   # Hora de ejecución del recordatorio (default: 9 AM todos los días)
 *   ecosalud.reminder.cron=0 0 9 * * *
 * </pre>
 *
 * <p>Los estados que reciben recordatorio son: {@code PENDIENTE} y {@code CONFIRMADA}.
 * Las citas {@code CANCELADA} y {@code COMPLETADA} son ignoradas.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderJob {

    private static final List<String> REMINDER_STATUSES = List.of("PENDIENTE", "CONFIRMADA");

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "CO"));
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("hh:mm a", new Locale("es", "CO"));

    private final TenantRepository      tenantRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository        userRepository;
    private final EmailService          emailService;
    private final SmsService            smsService;

    /**
     * Ejecuta los recordatorios de cita del día siguiente.
     *
     * <p>Cron configurable vía propiedad {@code ecosalud.reminder.cron}.
     * Por defecto corre a las <strong>09:00 AM</strong> todos los días.</p>
     */
    @Scheduled(cron = "${ecosalud.reminder.cron:0 0 9 * * *}")
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("[ReminderJob] Iniciando recordatorios para el día: {}", tomorrow);

        // Obtener todos los tenants activos con schema listo
        List<Tenant> tenants = tenantRepository.findAll().stream()
            .filter(t -> Boolean.TRUE.equals(t.getActive())
                      && Boolean.TRUE.equals(t.getSchemaInitialized()))
            .toList();

        int totalEnviados = 0;

        for (Tenant tenant : tenants) {
            try {
                // Establecer contexto del tenant para que Hibernate use el schema correcto
                TenantContext.setCurrentTenant(tenant.getSchemaName());
                int enviados = processTenant(tenant, tomorrow);
                totalEnviados += enviados;
            } catch (Exception e) {
                log.error("[ReminderJob] Error procesando tenant '{}': {}",
                    tenant.getSchemaName(), e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }

        log.info("[ReminderJob] Finalizado. Total recordatorios enviados: {}", totalEnviados);
    }

    /**
     * Procesa las citas de mañana para un tenant específico.
     *
     * @param tenant   tenant a procesar
     * @param tomorrow fecha de mañana
     * @return número de recordatorios de email enviados con éxito
     */
    private int processTenant(Tenant tenant, LocalDate tomorrow) {
        List<Appointment> appointments = appointmentRepository
            .findByAppointmentDateAndStatusIn(tomorrow, REMINDER_STATUSES);

        if (appointments.isEmpty()) {
            log.debug("[ReminderJob] Tenant '{}': sin citas para mañana.", tenant.getSchemaName());
            return 0;
        }

        log.info("[ReminderJob] Tenant '{}': {} cita(s) para mañana.",
            tenant.getSchemaName(), appointments.size());

        int enviados = 0;

        for (Appointment apt : appointments) {
            try {
                enviados += sendReminder(apt, tenant.getName());
            } catch (Exception e) {
                log.warn("[ReminderJob] Error enviando recordatorio para cita {}: {}",
                    apt.getId(), e.getMessage());
            }
        }

        return enviados;
    }

    /**
     * Envía el recordatorio (email + SMS/WhatsApp) para una cita específica.
     *
     * @param apt        la cita
     * @param clinicName nombre de la clínica del tenant
     * @return {@code 1} si el email se envió, {@code 0} si el paciente no existe
     */
    private int sendReminder(Appointment apt, String clinicName) {
        Optional<User> patientOpt = userRepository.findById(apt.getPatientId());
        if (patientOpt.isEmpty()) {
            log.warn("[ReminderJob] Cita {} tiene patientId={} que no existe en users.",
                apt.getId(), apt.getPatientId());
            return 0;
        }

        User patient = patientOpt.get();
        String dateTimeText = formatDateTime(apt);

        // ── Email (siempre, si hay API key de Resend configurada) ──
        emailService.sendAppointmentReminder(
            patient.getEmail(),
            patient.getName(),
            "cita médica",        // servicio — resolver con serviceId en Fase futura
            dateTimeText,
            clinicName
        );

        log.info("[ReminderJob] Email de recordatorio enviado → {} (cita {})",
            patient.getEmail(), apt.getId());

        // ── SMS / WhatsApp (solo si el paciente tiene teléfono) ──
        if (patient.getPhone() != null && !patient.getPhone().isBlank()) {
            smsService.sendAppointmentReminder(
                patient.getPhone(),
                patient.getName(),
                "cita médica",
                dateTimeText,
                clinicName
            );
        }

        return 1;
    }

    /**
     * Formatea la fecha y hora de la cita en español colombiano.
     * Ejemplo: {@code "Lunes 8 de junio de 2026 · 09:00 AM"}
     */
    private String formatDateTime(Appointment apt) {
        String date = apt.getAppointmentDate().format(DATE_FMT);
        if (apt.getAppointmentTime() != null) {
            String time = apt.getAppointmentTime().format(TIME_FMT);
            return date + " · " + time;
        }
        return date;
    }
}
