package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.NotificationStatusDTO;
import com.demo.ecosalud.model.dto.NotificationStatusDTO.UpcomingReminder;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controlador del panel de notificaciones automáticas.
 *
 * <p>Ruta base: {@code /api/notifications}</p>
 * <p>Solo accesible para usuarios autenticados del tenant activo (rol ADMIN).</p>
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   GET /api/notifications/status   → estado de los canales + lista próximas citas
 * </pre>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final List<String> ACTIVE_STATUSES = List.of("PENDIENTE", "CONFIRMADA");

    private final AppointmentRepository appointmentRepository;
    private final UserRepository        userRepository;
    private final SmsService            smsService;

    /** API key de Resend — solo verifica si está configurada. */
    @Value("${resend.api-key:}")
    private String resendApiKey;

    /** Cron del job de recordatorios — para mostrar al admin. */
    @Value("${ecosalud.reminder.cron:0 0 9 * * *}")
    private String reminderCron;

    /**
     * Retorna el estado de configuración de las notificaciones y la lista
     * de citas próximas que recibirán recordatorio automático.
     *
     * @return {@link NotificationStatusDTO} con canales configurados y upcoming list
     */
    @GetMapping("/status")
    public NotificationStatusDTO getStatus() {
        LocalDate today    = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate in7Days  = today.plusDays(7);

        // ── Citas de mañana ──────────────────────────────────────────────────
        List<Appointment> tomorrowAppts = appointmentRepository
            .findByAppointmentDateAndStatusIn(tomorrow, ACTIVE_STATUSES);

        // ── Citas próximos 7 días ────────────────────────────────────────────
        List<Appointment> next7Appts = appointmentRepository
            .findByAppointmentDateBetweenAndStatusInOrderByAppointmentDateAscAppointmentTimeAsc(
                tomorrow, in7Days, ACTIVE_STATUSES);

        // ── Pre-cargar pacientes en batch para evitar N+1 ────────────────────
        List<Long> patientIds = next7Appts.stream()
            .map(Appointment::getPatientId)
            .distinct()
            .toList();

        Map<Long, User> patients = userRepository.findAllById(patientIds)
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        // ── Construir lista de upcoming reminders ────────────────────────────
        boolean emailEnabled    = !resendApiKey.isBlank();
        boolean smsEnabled      = smsService.isSmsEnabled();
        boolean whatsAppEnabled = smsService.isWhatsAppEnabled();

        List<UpcomingReminder> upcoming = next7Appts.stream()
            .map(apt -> {
                User patient = patients.get(apt.getPatientId());
                boolean hasPhone = patient != null
                    && patient.getPhone() != null
                    && !patient.getPhone().isBlank();

                return UpcomingReminder.builder()
                    .appointmentId(apt.getId())
                    .patientName(patient != null ? patient.getName() : "Paciente #" + apt.getPatientId())
                    .patientEmail(patient != null ? patient.getEmail() : "—")
                    .patientHasPhone(hasPhone)
                    .date(apt.getAppointmentDate().toString())
                    .time(apt.getAppointmentTime() != null
                        ? apt.getAppointmentTime().toString().substring(0, 5)
                        : "—")
                    .status(apt.getStatus())
                    .emailWillBeSent(emailEnabled && patient != null)
                    .smsWillBeSent((smsEnabled || whatsAppEnabled) && hasPhone)
                    .build();
            })
            .toList();

        return NotificationStatusDTO.builder()
            .emailEnabled(emailEnabled)
            .smsEnabled(smsEnabled)
            .whatsAppEnabled(whatsAppEnabled)
            .reminderCron(reminderCron)
            .reminderCronDescription(describeCron(reminderCron))
            .tomorrowCount(tomorrowAppts.size())
            .next7DaysCount(next7Appts.size())
            .upcoming(upcoming)
            .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Genera una descripción legible del cron en español.
     * Cubre los casos comunes de Ecosalud.
     */
    private String describeCron(String cron) {
        if (cron == null) return "No configurado";
        // Formato esperado: "0 {min} {hour} * * *"
        String[] parts = cron.split("\\s+");
        if (parts.length >= 6) {
            try {
                String hour = parts[2];
                String min  = parts[1];
                return "Todos los días a las " + hour + ":" + (min.length() == 1 ? "0" + min : min) + " (hora servidor)";
            } catch (Exception ignored) { /* fall through */ }
        }
        return cron;
    }
}
