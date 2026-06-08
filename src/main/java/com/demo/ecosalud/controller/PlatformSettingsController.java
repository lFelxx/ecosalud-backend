package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.entities.PlatformSettings;
import com.demo.ecosalud.repository.PlatformSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuración global de la plataforma.
 *
 * <p>GET  {@code /api/public/settings}        — público (frontend, footer, páginas legales)</p>
 * <p>PUT  {@code /api/platform/settings}       — super-admin (actualiza un setting por clave)</p>
 * <p>POST {@code /api/platform/settings/init}  — super-admin (inicializa defaults)</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PlatformSettingsController {

    private final PlatformSettingsRepository repo;

    /** Valores predeterminados — se insertan si la clave no existe aún. */
    private static final Map<String, String[]> DEFAULTS = new LinkedHashMap<>() {{
        put("social.whatsapp",  new String[]{ "",  "Número WhatsApp con código de país (ej. +573001234567)" });
        put("social.email",     new String[]{ "",  "Email de contacto visible en el footer" });
        put("social.instagram", new String[]{ "",  "URL del perfil de Instagram" });
        put("social.facebook",  new String[]{ "",  "URL del perfil de Facebook" });
        put("social.twitter",   new String[]{ "",  "URL del perfil de Twitter/X" });
        put("contact.email",    new String[]{ "soporte@ecosalud.com", "Email de soporte al cliente" });
        put("contact.phone",    new String[]{ "+57 (601) 123-4567",   "Teléfono de soporte" });
        put("contact.address",  new String[]{ "Bogotá, Colombia",     "Dirección física" });
        put("contact.schedule", new String[]{ "Lunes a Viernes, 8:00 AM – 6:00 PM (COT)", "Horario de atención" });
        put("legal.privacy",    new String[]{ DEFAULT_PRIVACY,  "Política de privacidad (texto completo)" });
        put("legal.terms",      new String[]{ DEFAULT_TERMS,    "Términos y condiciones (texto completo)" });
    }};

    // ═══════════════════════════════════════════════════════
    // PÚBLICO
    // ═══════════════════════════════════════════════════════

    /**
     * Retorna todos los settings como un mapa key→value.
     * El frontend usa este mapa para renderizar footer, páginas legales y contacto.
     */
    @GetMapping("/api/public/settings")
    public Map<String, String> getAll() {
        Map<String, String> map = new LinkedHashMap<>();
        repo.findAll().forEach(s -> map.put(s.getSettingKey(), s.getSettingValue() != null ? s.getSettingValue() : ""));
        return map;
    }

    // ═══════════════════════════════════════════════════════
    // SUPER-ADMIN
    // ═══════════════════════════════════════════════════════

    /** Lista todos los settings con metadata (para el panel super-admin). */
    @GetMapping("/api/platform/settings")
    public List<PlatformSettings> listAll() {
        return repo.findAll();
    }

    /** Actualiza (o crea) un setting por clave. */
    @PutMapping("/api/platform/settings/{key}")
    public ResponseEntity<PlatformSettings> upsert(
            @PathVariable("key") String key,
            @RequestBody Map<String, String> body) {

        String value = body.getOrDefault("value", "");
        PlatformSettings setting = repo.findBySettingKey(key)
            .orElseGet(() -> PlatformSettings.builder()
                .settingKey(key)
                .description(body.getOrDefault("description", ""))
                .build());
        setting.setSettingValue(value);
        log.info("[PlatformSettings] Clave '{}' actualizada.", key);
        return ResponseEntity.ok(repo.save(setting));
    }

    /**
     * Inicializa todos los settings con valores por defecto (idempotente).
     * Solo inserta claves que no existen — no sobreescribe valores existentes.
     */
    @PostMapping("/api/platform/settings/init")
    public Map<String, String> initDefaults() {
        int created = 0;
        for (Map.Entry<String, String[]> entry : DEFAULTS.entrySet()) {
            if (repo.findBySettingKey(entry.getKey()).isEmpty()) {
                repo.save(PlatformSettings.builder()
                    .settingKey(entry.getKey())
                    .settingValue(entry.getValue()[0])
                    .description(entry.getValue()[1])
                    .build());
                created++;
            }
        }
        log.info("[PlatformSettings] Inicialización — {} settings creados.", created);
        return Map.of("created", String.valueOf(created), "status", "ok");
    }

    // ═══════════════════════════════════════════════════════
    // Contenido predeterminado de páginas legales
    // ═══════════════════════════════════════════════════════

    private static final String DEFAULT_PRIVACY = """
Política de Privacidad — Ecosalud Market

Última actualización: Junio 2026

1. INFORMACIÓN QUE RECOPILAMOS
Ecosalud Market recopila información que usted nos proporciona directamente al registrarse: nombre, dirección de correo electrónico, información de la clínica, especialidad y datos de contacto.

2. USO DE LA INFORMACIÓN
Utilizamos la información recopilada para: proporcionar, mantener y mejorar nuestros servicios; procesar transacciones; enviar información técnica y de soporte; responder comentarios y preguntas; y cumplir con obligaciones legales.

3. SEGURIDAD DE LOS DATOS
Implementamos medidas de seguridad técnicas y organizacionales apropiadas para proteger su información personal contra acceso no autorizado, alteración, divulgación o destrucción.

4. RETENCIÓN DE DATOS
Conservamos su información mientras su cuenta esté activa o según sea necesario para proporcionar servicios. Los datos de historia clínica se conservan por 20 años conforme a la Resolución 1995/1999 del MinSalud de Colombia.

5. SUS DERECHOS (Ley 1581/2012 — Habeas Data)
Usted tiene derecho a acceder, rectificar, cancelar y oponerse al tratamiento de sus datos personales. Para ejercer estos derechos, contáctenos.

6. CONTACTO
Para preguntas sobre esta política, escríbanos a soporte@ecosalud.com.
""";

    private static final String DEFAULT_TERMS = """
Términos y Condiciones de Uso — Ecosalud Market

Última actualización: Junio 2026

1. ACEPTACIÓN DE TÉRMINOS
Al acceder y utilizar la plataforma Ecosalud Market, usted acepta estos Términos y Condiciones. Si no está de acuerdo, no utilice el servicio.

2. DESCRIPCIÓN DEL SERVICIO
Ecosalud Market es una plataforma SaaS que permite a profesionales de salud gestionar su agenda, historia clínica electrónica, pagos y comunicaciones con pacientes.

3. REGISTRO Y CUENTA
Usted es responsable de mantener la confidencialidad de sus credenciales de acceso y de todas las actividades que ocurran bajo su cuenta.

4. PERÍODO DE PRUEBA
El período de prueba gratuito de 14 días es válido una sola vez por dirección de correo electrónico. Una vez vencido, debe adquirir un plan para continuar usando el servicio.

5. PAGOS Y FACTURACIÓN
Los planes de suscripción se cobran mensualmente. Los pagos se procesan a través de PayU Colombia. No se realizan reembolsos por períodos parciales.

6. DATOS DE PACIENTES
Usted es el responsable del tratamiento de los datos de sus pacientes conforme a la Ley 1581/2012 y la normativa del MinSalud. Ecosalud actúa como encargado del tratamiento.

7. PROPIEDAD INTELECTUAL
Todos los derechos sobre la plataforma, incluyendo el software, diseño y marca, pertenecen a Ecosalud Market. Usted recibe una licencia de uso limitada y no exclusiva.

8. LIMITACIÓN DE RESPONSABILIDAD
Ecosalud Market no será responsable por daños indirectos, incidentales o consecuentes derivados del uso del servicio.

9. MODIFICACIONES
Nos reservamos el derecho de modificar estos términos en cualquier momento. Le notificaremos por email con al menos 30 días de anticipación.

10. CONTACTO LEGAL
Para consultas legales: soporte@ecosalud.com — Bogotá, Colombia.
""";
}
