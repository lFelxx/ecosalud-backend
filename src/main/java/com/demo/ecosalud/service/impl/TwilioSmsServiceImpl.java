package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Implementación del servicio de SMS/WhatsApp usando la API REST de <strong>Twilio</strong>.
 *
 * <p><strong>Sin dependencia de SDK</strong> — usa {@code RestTemplate} + HTTP Basic Auth,
 * lo que evita agregar el SDK de Twilio (~30 MB) al classpath.</p>
 *
 * <h3>Configuración</h3>
 * <pre>
 *   twilio.account-sid=${TWILIO_ACCOUNT_SID:}
 *   twilio.auth-token=${TWILIO_AUTH_TOKEN:}
 *   twilio.from-number=${TWILIO_FROM_NUMBER:}
 *   twilio.whatsapp-from=${TWILIO_WHATSAPP_FROM:}
 * </pre>
 *
 * <h3>Flujo de envío</h3>
 * <pre>
 *   POST https://api.twilio.com/2010-04-01/Accounts/{AccountSid}/Messages.json
 *   Authorization: Basic base64(AccountSid:AuthToken)
 *   Content-Type: application/x-www-form-urlencoded
 *   Body: From=+15005550006&To=+573001234567&Body=Tu+cita+es+mañana
 * </pre>
 *
 * <h3>WhatsApp</h3>
 * <p>Para WhatsApp, {@code From} debe tener el prefijo {@code whatsapp:}
 * y el número debe estar activo en Twilio WhatsApp Sandbox o WhatsApp Business API.</p>
 *
 * @see <a href="https://www.twilio.com/docs/sms/api">Twilio SMS API</a>
 * @see <a href="https://www.twilio.com/docs/whatsapp/api">Twilio WhatsApp API</a>
 */
@Slf4j
@Service
public class TwilioSmsServiceImpl implements SmsService {

    private static final String TWILIO_API_BASE =
        "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json";

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    /** Número Twilio para SMS (p.ej. {@code +15005550006}). */
    @Value("${twilio.from-number:}")
    private String fromNumber;

    /** Número WhatsApp Business en Twilio (p.ej. {@code whatsapp:+14155238886}). */
    @Value("${twilio.whatsapp-from:}")
    private String whatsAppFrom;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Estado del servicio ───────────────────────────────────────────────────

    @Override
    public boolean isSmsEnabled() {
        return !accountSid.isBlank() && !authToken.isBlank() && !fromNumber.isBlank();
    }

    @Override
    public boolean isWhatsAppEnabled() {
        return isSmsEnabled() && !whatsAppFrom.isBlank();
    }

    // ── Envío de SMS ──────────────────────────────────────────────────────────

    @Override
    public void sendSms(String toPhone, String body) {
        if (toPhone == null || toPhone.isBlank()) {
            log.debug("[SMS] Número vacío — omitiendo envío.");
            return;
        }
        if (!isSmsEnabled()) {
            log.info("[SMS - MODO DEV] SMS NO enviado (Twilio no configurado):");
            log.info("  Para  : {}", toPhone);
            log.info("  Cuerpo: {}", body);
            return;
        }
        sendTwilio(fromNumber, toPhone, body);
    }

    @Override
    public void sendWhatsApp(String toPhone, String body) {
        if (toPhone == null || toPhone.isBlank()) {
            log.debug("[WhatsApp] Número vacío — omitiendo envío.");
            return;
        }
        if (!isWhatsAppEnabled()) {
            log.info("[WhatsApp - MODO DEV] Mensaje NO enviado (WhatsApp Twilio no configurado):");
            log.info("  Para  : {}", toPhone);
            log.info("  Cuerpo: {}", body);
            return;
        }
        // Los números WhatsApp deben tener prefijo "whatsapp:"
        String waTo = toPhone.startsWith("whatsapp:") ? toPhone : "whatsapp:" + toPhone;
        sendTwilio(whatsAppFrom, waTo, body);
    }

    @Override
    public void sendAppointmentReminder(String toPhone, String patientName,
                                         String service, String dateTime, String clinicName) {
        if (toPhone == null || toPhone.isBlank()) {
            log.debug("[SMS] Paciente sin teléfono — omitiendo SMS.");
            return;
        }

        String message = buildReminderText(patientName, service, dateTime, clinicName);

        // WhatsApp tiene mayor tasa de apertura → intentamos primero
        if (isWhatsAppEnabled()) {
            sendWhatsApp(toPhone, message);
        } else if (isSmsEnabled()) {
            sendSms(toPhone, message);
        } else {
            log.info("[SMS - MODO DEV] Recordatorio SMS/WhatsApp no enviado (sin configuración Twilio).");
            log.info("  Para: {} · Mensaje: {}", toPhone, message);
        }
    }

    // ── Lógica central de Twilio ──────────────────────────────────────────────

    /**
     * Realiza el POST a la API de Twilio con autenticación Basic.
     *
     * @param from  número origen (SMS: {@code +15005550006}, WA: {@code whatsapp:+14155238886})
     * @param to    número destino
     * @param body  cuerpo del mensaje
     */
    private void sendTwilio(String from, String to, String body) {
        try {
            String url = TWILIO_API_BASE.formatted(accountSid);

            // Autenticación: Basic base64(AccountSid:AuthToken)
            String credentials = accountSid + ":" + authToken;
            String basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set(HttpHeaders.AUTHORIZATION, basicAuth);

            // Cuerpo form-encoded
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("From", from);
            formData.add("To",   to);
            formData.add("Body", body);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Twilio] Mensaje enviado correctamente a: {}", to);
            } else {
                log.warn("[Twilio] Respuesta inesperada: {} — {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            // El error de SMS no debe romper el flujo principal
            log.error("[Twilio] Error al enviar mensaje a {}: {}", to, e.getMessage());
        }
    }

    // ── Plantilla de texto ────────────────────────────────────────────────────

    /**
     * Genera el texto del recordatorio de cita para SMS/WhatsApp.
     * Mantiene el mensaje bajo 160 caracteres para un solo SMS.
     */
    private String buildReminderText(String nombre, String servicio,
                                      String fechaHora, String clinica) {
        return "🌿 *Ecosalud* - Recordatorio\n\n"
             + "Hola %s, mañana tienes cita de *%s* en %s.\n"
             + "📅 %s\n\n"
             + "¿Necesitas cancelar? Hazlo con 24h de anticipación."
             .formatted(nombre, servicio, clinica, fechaHora);
    }
}
