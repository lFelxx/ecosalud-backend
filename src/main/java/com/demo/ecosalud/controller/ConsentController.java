package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.ConsentDTO;
import com.demo.ecosalud.service.ConsentService;
import com.demo.ecosalud.service.impl.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consentimientos informados digitales.
 *
 * <p>Ruta base: {@code /api/consents}</p>
 *
 * <ul>
 *   <li>GET  /api/consents        — Admin: todos los consentimientos del tenant</li>
 *   <li>GET  /api/consents/my     — Paciente: sus propios consentimientos</li>
 *   <li>GET  /api/consents/{id}   — Detalle de un consentimiento</li>
 *   <li>POST /api/consents/sign   — Paciente: firma un nuevo consentimiento</li>
 * </ul>
 *
 * <p>Los consentimientos son <b>inmutables</b> una vez firmados: no existe
 * endpoint de actualización ni eliminación.</p>
 */
@RestController
@RequestMapping("/api/consents")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService service;

    /** Lista todos los consentimientos del tenant. Solo admin/editor. */
    @GetMapping
    public List<ConsentDTO> listAll() {
        return service.getAll();
    }

    /** Retorna los consentimientos del paciente autenticado. */
    @GetMapping("/my")
    public List<ConsentDTO> myConsents(@AuthenticationPrincipal UserDetailsImpl principal) {
        return service.getByPatientId(principal.getUser().getId());
    }

    /** Detalle de un consentimiento por ID. */
    @GetMapping("/{id}")
    public ConsentDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * El paciente firma un consentimiento informado.
     *
     * <p><b>Seguridad:</b> el {@code patientId} se toma siempre del JWT.
     * La IP se captura del request para trazabilidad legal.</p>
     *
     * <p>Body esperado:
     * <pre>{ "serviceId": 3, "digitalSignature": "María García López" }</pre>
     * </p>
     */
    @PostMapping("/sign")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsentDTO sign(
            @RequestBody ConsentDTO dto,
            @AuthenticationPrincipal UserDetailsImpl principal,
            HttpServletRequest request) {

        Long patientId = principal.getUser().getId();
        String ip      = resolveClientIp(request);
        return service.sign(patientId, dto, ip);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Resuelve la IP real del cliente teniendo en cuenta proxies / CDN.
     * Revisa los headers estándar antes de usar la IP de conexión.
     */
    private String resolveClientIp(HttpServletRequest req) {
        for (String header : new String[]{ "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP" }) {
            String ip = req.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim(); // X-Forwarded-For puede ser lista
            }
        }
        return req.getRemoteAddr();
    }
}
