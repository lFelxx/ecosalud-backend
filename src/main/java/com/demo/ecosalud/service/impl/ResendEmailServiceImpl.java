package com.demo.ecosalud.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.demo.ecosalud.service.EmailService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de email usando la API REST de Resend.
 *
 * <p>Resend es el proveedor de email transaccional de la plataforma Ecosalud.
 * Plan gratuito: 3,000 emails/mes — suficiente para el arranque.</p>
 *
 * <p><strong>Configuración:</strong></p>
 * <pre>
 *   # En application.properties (o variables de entorno):
 *   resend.api-key=re_xxxxxxxxxxxxxxx
 *   resend.from-email=noreply@ecosalud.com
 *   resend.from-name=Ecosalud
 * </pre>
 *
 * <p>Si la API key está vacía, los emails se loguean en consola
 * (modo desarrollo sin envío real).</p>
 *
 * <p>Endpoint de Resend: {@code POST https://api.resend.com/emails}</p>
 */
@Slf4j
@Service
public class ResendEmailServiceImpl implements EmailService {

    /** URL base de la API de Resend */
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:noreply@ecosalud.com}")
    private String fromEmail;

    @Value("${resend.from-name:Ecosalud}")
    private String fromName;

    // RestTemplate para llamadas HTTP a la API de Resend
    private final RestTemplate restTemplate = new RestTemplate();

    // ── Implementaciones ─────────────────────────────────────────────────────

    @Override
    public void sendAppointmentConfirmation(String toEmail, String patientName,
            String serviceName, String dateTime, String specialistName, String clinicName) {

        // Asunto y cuerpo del email de confirmación
        String asunto = "✅ Tu cita en " + clinicName + " ha sido confirmada";
        String html = buildConfirmationHtml(patientName, serviceName, dateTime, specialistName, clinicName);
        enviar(toEmail, asunto, html);
    }

    @Override
    public void sendAppointmentReminder(String toEmail, String patientName,
            String serviceName, String dateTime, String clinicName) {

        // Recordatorio enviado 24h antes de la cita
        String asunto = "🔔 Recordatorio: tu cita de " + serviceName + " es mañana";
        String html = buildReminderHtml(patientName, serviceName, dateTime, clinicName);
        enviar(toEmail, asunto, html);
    }

    @Override
    public void sendWelcomeCredentials(String toEmail, String patientName,
            String password, String clinicName, String loginUrl) {

        // Email de bienvenida con credenciales temporales
        String asunto = "🌿 Bienvenido/a a " + clinicName + " — Tus credenciales de acceso";
        String html = buildWelcomeHtml(patientName, toEmail, password, clinicName, loginUrl);
        enviar(toEmail, asunto, html);
    }

    @Override
    public void sendAppointmentCancellation(String toEmail, String patientName,
            String serviceName, String dateTime, String cancellationNote) {

        // Notificación de cancelación
        String asunto = "❌ Tu cita de " + serviceName + " ha sido cancelada";
        String html = buildCancellationHtml(patientName, serviceName, dateTime, cancellationNote);
        enviar(toEmail, asunto, html);
    }

    @Override
    public void sendTrialExpiring(String toEmail, String ownerName,
            String clinicName, int daysLeft, String billingUrl) {

        String asunto = "⏳ Tu período de prueba vence en " + daysLeft
                + (daysLeft == 1 ? " día" : " días") + " — " + clinicName;
        String html = buildTrialExpiringHtml(ownerName, clinicName, daysLeft, billingUrl);
        enviar(toEmail, asunto, html);
    }

    @Override
    public void sendTrialExpired(String toEmail, String ownerName,
            String clinicName, String billingUrl) {

        String asunto = "🔒 Tu período de prueba ha vencido — " + clinicName;
        String html = buildTrialExpiredHtml(ownerName, clinicName, billingUrl);
        enviar(toEmail, asunto, html);
    }

    // ── Método central de envío ──────────────────────────────────────────────

    /**
     * Realiza la llamada HTTP POST a la API de Resend.
     * Si la API key no está configurada, el email se loguea en consola (modo dev).
     *
     * @param toEmail destinatario
     * @param subject asunto del email
     * @param html    cuerpo HTML del email
     */
    private void enviar(String toEmail, String subject, String html) {
        // Si no hay API key, modo desarrollo: solo logueamos
        if (apiKey == null || apiKey.isBlank()) {
            log.info("[EmailService - MODO DEV] Email NO enviado (sin API key):");
            log.info("  Para   : {}", toEmail);
            log.info("  Asunto : {}", subject);
            log.info("  --- contenido HTML no mostrado en log ---");
            return;
        }

        try {
            // Construir headers con autenticación Bearer para Resend
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Cuerpo del request según la API de Resend
            Map<String, Object> body = Map.of(
                    "from",    fromName + " <" + fromEmail + ">",
                    "to",      new String[]{ toEmail },
                    "subject", subject,
                    "html",    html
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[EmailService] Email enviado correctamente a: {}", toEmail);
            } else {
                log.warn("[EmailService] Respuesta inesperada de Resend: {} — {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            // El error de email no debe romper el flujo principal de la app
            log.error("[EmailService] Error al enviar email a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Plantillas HTML ──────────────────────────────────────────────────────

    /**
     * Plantilla HTML para confirmación de cita.
     * Diseño minimalista en verde teal (#3DAA96) — colores de Ecosalud.
     */
    private String buildConfirmationHtml(String nombre, String servicio,
            String fechaHora, String especialista, String clinica) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                <body style="font-family: 'Helvetica Neue', Arial, sans-serif; background:#F7FAF9; margin:0; padding:20px;">
                  <div style="max-width:520px; margin:0 auto; background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                    <!-- Cabecera -->
                    <div style="background:linear-gradient(135deg,#1A3E38,#3DAA96); padding:32px 28px; text-align:center;">
                      <h1 style="color:#fff; margin:0; font-size:22px; font-weight:800;">✅ ¡Cita Confirmada!</h1>
                      <p style="color:rgba(255,255,255,0.85); margin:8px 0 0; font-size:14px;">%s</p>
                    </div>
                    <!-- Contenido -->
                    <div style="padding:28px;">
                      <p style="color:#2C3E35; font-size:15px;">Hola <strong>%s</strong>,</p>
                      <p style="color:#4A6B60; font-size:14px; line-height:1.7;">Tu cita ha sido registrada exitosamente. Te esperamos pronto.</p>
                      <!-- Detalles -->
                      <div style="background:#F4FAF8; border-radius:8px; padding:20px; margin:20px 0;">
                        <div style="margin-bottom:12px;">
                          <span style="font-size:11px; font-weight:700; color:#9DBFBA; letter-spacing:0.8px; text-transform:uppercase;">Terapia</span>
                          <p style="margin:4px 0 0; color:#1A2E2A; font-weight:700; font-size:15px;">%s</p>
                        </div>
                        <div style="margin-bottom:12px;">
                          <span style="font-size:11px; font-weight:700; color:#9DBFBA; letter-spacing:0.8px; text-transform:uppercase;">Fecha y hora</span>
                          <p style="margin:4px 0 0; color:#1A2E2A; font-weight:700; font-size:15px;">%s</p>
                        </div>
                        <div>
                          <span style="font-size:11px; font-weight:700; color:#9DBFBA; letter-spacing:0.8px; text-transform:uppercase;">Especialista</span>
                          <p style="margin:4px 0 0; color:#1A2E2A; font-weight:700; font-size:15px;">%s</p>
                        </div>
                      </div>
                      <p style="color:#9DBFBA; font-size:13px; text-align:center;">Si necesitas cancelar o reprogramar, contáctanos con al menos 24 horas de anticipación.</p>
                    </div>
                    <!-- Pie -->
                    <div style="background:#F4FAF8; padding:16px 28px; text-align:center; border-top:1px solid #E3EFEC;">
                      <p style="color:#9DBFBA; font-size:12px; margin:0;">Este email fue enviado por Ecosalud · No respondas a este correo</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(clinica, nombre, servicio, fechaHora, especialista);
    }

    /** Plantilla HTML para recordatorio de cita (24h antes). */
    private String buildReminderHtml(String nombre, String servicio, String fechaHora, String clinica) {
        return """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#F7FAF9;padding:20px;">
                <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                  <div style="background:#3DAA96;padding:28px;text-align:center;">
                    <h1 style="color:#fff;margin:0;font-size:20px;">🔔 Recordatorio de cita</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:6px 0 0;font-size:14px;">%s</p>
                  </div>
                  <div style="padding:28px;">
                    <p style="color:#2C3E35;font-size:15px;">Hola <strong>%s</strong>,</p>
                    <p style="color:#4A6B60;font-size:14px;line-height:1.7;">
                      Te recordamos que mañana tienes una cita programada de <strong>%s</strong>.
                    </p>
                    <div style="background:#F4FAF8;border-radius:8px;padding:16px;margin:16px 0;text-align:center;">
                      <p style="color:#3DAA96;font-weight:800;font-size:18px;margin:0;">%s</p>
                    </div>
                    <p style="color:#9DBFBA;font-size:13px;text-align:center;">¿Necesitas cancelar? Hazlo con al menos 24h de anticipación.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(clinica, nombre, servicio, fechaHora);
    }

    /** Plantilla HTML para bienvenida con credenciales. */
    private String buildWelcomeHtml(String nombre, String email, String password,
            String clinica, String loginUrl) {
        return """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#F7FAF9;padding:20px;">
                <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                  <div style="background:linear-gradient(135deg,#1A3E38,#3DAA96);padding:32px 28px;text-align:center;">
                    <h1 style="color:#fff;margin:0;font-size:22px;">🌿 Bienvenido/a a %s</h1>
                  </div>
                  <div style="padding:28px;">
                    <p style="color:#2C3E35;font-size:15px;">Hola <strong>%s</strong>,</p>
                    <p style="color:#4A6B60;font-size:14px;line-height:1.7;">
                      El especialista ha creado tu perfil. Aquí están tus credenciales de acceso:
                    </p>
                    <div style="background:#F4FAF8;border-radius:8px;padding:20px;margin:16px 0;">
                      <p style="margin:0 0 8px;"><strong style="color:#9DBFBA;font-size:11px;text-transform:uppercase;letter-spacing:0.8px;">Usuario (email)</strong><br>
                        <span style="color:#1A2E2A;font-size:15px;font-weight:700;">%s</span></p>
                      <p style="margin:0;"><strong style="color:#9DBFBA;font-size:11px;text-transform:uppercase;letter-spacing:0.8px;">Contraseña temporal</strong><br>
                        <span style="color:#3DAA96;font-size:18px;font-weight:800;font-family:monospace;">%s</span></p>
                    </div>
                    <div style="text-align:center;margin:24px 0;">
                      <a href="%s" style="background:#3DAA96;color:#fff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:700;font-size:15px;">Iniciar sesión →</a>
                    </div>
                    <p style="color:#C0392B;font-size:13px;text-align:center;">⚠️ Por seguridad, cambia tu contraseña al primer inicio de sesión.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(clinica, nombre, email, password, loginUrl);
    }

    /** Plantilla HTML para notificación de cancelación. */
    private String buildCancellationHtml(String nombre, String servicio,
            String fechaHora, String motivo) {
        String motivoHtml = (motivo != null && !motivo.isBlank())
                ? "<p style=\"color:#4A6B60;font-size:14px;\">Motivo: <em>" + motivo + "</em></p>"
                : "";
        return """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#F7FAF9;padding:20px;">
                <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                  <div style="background:#C0392B;padding:28px;text-align:center;">
                    <h1 style="color:#fff;margin:0;font-size:20px;">❌ Cita cancelada</h1>
                  </div>
                  <div style="padding:28px;">
                    <p style="color:#2C3E35;font-size:15px;">Hola <strong>%s</strong>,</p>
                    <p style="color:#4A6B60;font-size:14px;line-height:1.7;">
                      Tu cita de <strong>%s</strong> programada para <strong>%s</strong> ha sido cancelada.
                    </p>
                    %s
                    <p style="color:#9DBFBA;font-size:13px;text-align:center;">¿Deseas reagendar? Contáctanos o visita nuestra plataforma.</p>
                  </div>
                </div>
                </body></html>
                """.formatted(nombre, servicio, fechaHora, motivoHtml);
    }

    /** Plantilla HTML — aviso de trial próximo a vencer (3 días antes). */
    private String buildTrialExpiringHtml(String ownerName, String clinicName,
            int daysLeft, String billingUrl) {
        String diasTexto = daysLeft == 1 ? "1 día" : daysLeft + " días";
        return """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#F7FAF9;padding:20px;">
                <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                  <div style="background:linear-gradient(135deg,#B86000,#E67E22);padding:32px 28px;text-align:center;">
                    <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;">⏳ Tu prueba gratuita vence pronto</h1>
                    <p style="color:rgba(255,255,255,0.9);margin:8px 0 0;font-size:14px;">%s</p>
                  </div>
                  <div style="padding:28px;">
                    <p style="color:#2C3E35;font-size:15px;">Hola <strong>%s</strong>,</p>
                    <p style="color:#4A6B60;font-size:14px;line-height:1.7;">
                      Tu período de prueba gratuito en <strong>%s</strong> vence en
                      <strong style="color:#E67E22;">%s</strong>.
                    </p>
                    <p style="color:#4A6B60;font-size:14px;line-height:1.7;">
                      Cuando expire, la creación de nuevas citas y el registro de pacientes quedará
                      bloqueado hasta que actives una suscripción.
                    </p>
                    <div style="background:#FFF8E7;border:1.5px solid #F5A623;border-radius:8px;padding:16px;margin:20px 0;">
                      <p style="margin:0;color:#7D4B00;font-size:14px;font-weight:700;">
                        💡 Activa tu plan ahora y mantén el acceso sin interrupciones.
                      </p>
                    </div>
                    <div style="text-align:center;margin:24px 0;">
                      <a href="%s" style="background:#E67E22;color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:800;font-size:15px;">
                        Activar suscripción →
                      </a>
                    </div>
                    <p style="color:#9DBFBA;font-size:12px;text-align:center;margin-top:16px;">
                      Planes desde $119.000 COP/mes · Cancela en cualquier momento
                    </p>
                  </div>
                  <div style="background:#F4FAF8;padding:16px 28px;text-align:center;border-top:1px solid #E3EFEC;">
                    <p style="color:#9DBFBA;font-size:12px;margin:0;">Ecosalud Market · soporte@ecosalud.com</p>
                  </div>
                </div>
                </body></html>
                """.formatted(clinicName, ownerName, clinicName, diasTexto, billingUrl);
    }

    /** Plantilla HTML — aviso de trial vencido (cuenta pasa a OVERDUE). */
    private String buildTrialExpiredHtml(String ownerName, String clinicName, String billingUrl) {
        return """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,sans-serif;background:#F7FAF9;padding:20px;">
                <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">
                  <div style="background:linear-gradient(135deg,#6B0000,#C0392B);padding:32px 28px;text-align:center;">
                    <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;">🔒 Período de prueba vencido</h1>
                    <p style="color:rgba(255,255,255,0.9);margin:8px 0 0;font-size:14px;">%s</p>
                  </div>
                  <div style="padding:28px;">
                    <p style="color:#2C3E35;font-size:15px;">Hola <strong>%s</strong>,</p>
                    <p style="color:#4A6B60;font-size:14px;line-height:1.7;">
                      El período de prueba gratuito de <strong>%s</strong> ha vencido.
                      Tu cuenta está actualmente en estado <strong style="color:#C0392B;">Vencida</strong>.
                    </p>
                    <div style="background:#FDE8E8;border:1.5px solid #C0392B;border-radius:8px;padding:16px;margin:20px 0;">
                      <p style="margin:0 0 8px;color:#7D0000;font-size:14px;font-weight:700;">¿Qué significa esto?</p>
                      <ul style="margin:0;padding-left:18px;color:#7D0000;font-size:13px;line-height:1.8;">
                        <li>Los datos de tus pacientes y citas existentes están seguros.</li>
                        <li>No puedes crear nuevas citas ni registrar nuevos pacientes.</li>
                        <li>Al activar tu plan, el acceso completo se restaura inmediatamente.</li>
                      </ul>
                    </div>
                    <div style="text-align:center;margin:24px 0;">
                      <a href="%s" style="background:#3DAA96;color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:800;font-size:15px;">
                        Activar suscripción →
                      </a>
                    </div>
                    <p style="color:#9DBFBA;font-size:12px;text-align:center;margin-top:16px;">
                      ¿Tienes preguntas? Escríbenos a soporte@ecosalud.com
                    </p>
                  </div>
                  <div style="background:#F4FAF8;padding:16px 28px;text-align:center;border-top:1px solid #E3EFEC;">
                    <p style="color:#9DBFBA;font-size:12px;margin:0;">Ecosalud Market · soporte@ecosalud.com</p>
                  </div>
                </div>
                </body></html>
                """.formatted(clinicName, ownerName, clinicName, billingUrl);
    }
}
