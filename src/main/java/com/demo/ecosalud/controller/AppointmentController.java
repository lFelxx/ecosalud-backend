package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.service.AppointmentService;
import com.demo.ecosalud.service.PlanLimitsService;
import com.demo.ecosalud.service.impl.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para citas médicas.
 *
 * <p>Ruta base: {@code /api/appointments}</p>
 *
 * <table>
 *   <tr><th>Método</th><th>Ruta</th><th>Rol mínimo</th><th>Descripción</th></tr>
 *   <tr><td>GET</td><td>/api/appointments</td><td>ADMIN/EDITOR: todas · USER: solo las propias</td><td>Lista citas</td></tr>
 *   <tr><td>GET</td><td>/api/appointments/{id}</td><td>autenticado</td><td>Detalle</td></tr>
 *   <tr><td>POST</td><td>/api/appointments</td><td>ADMIN/EDITOR</td><td>Crear cita (admin)</td></tr>
 *   <tr><td>POST</td><td>/api/appointments/book</td><td>cualquier rol autenticado</td><td>Auto-agenda del paciente — patientId forzado del JWT</td></tr>
 *   <tr><td>PUT</td><td>/api/appointments/{id}</td><td>ADMIN/EDITOR</td><td>Actualizar</td></tr>
 *   <tr><td>PATCH</td><td>/api/appointments/{id}/status</td><td>ADMIN/EDITOR</td><td>Cambiar estado + email</td></tr>
 *   <tr><td>DELETE</td><td>/api/appointments/{id}</td><td>ADMIN/EDITOR</td><td>Eliminar</td></tr>
 * </table>
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;
    private final PlanLimitsService  planLimits;

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Lista citas con aislamiento de rol:
     * <ul>
     *   <li>USER (paciente): solo ve sus propias citas, ignorando el parámetro {@code patientId}.</li>
     *   <li>ADMIN/EDITOR: ve todas o filtra por {@code ?patientId=N}.</li>
     * </ul>
     */
    @GetMapping
    public List<AppointmentDTO> list(
            @RequestParam(required = false) Long patientId,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        // Pacientes solo acceden a sus propias citas
        if (principal != null && isPatientRole(principal)) {
            return service.getByPatientId(principal.getUser().getId());
        }

        return patientId != null ? service.getByPatientId(patientId) : service.getAll();
    }

    @GetMapping("/{id}")
    public AppointmentDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // ── Creación ──────────────────────────────────────────────────────────────

    /**
     * Crea una cita desde el panel de administración.
     * Permite especificar cualquier {@code patientId}.
     * Uso exclusivo de roles ADMIN y EDITOR.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDTO create(
            @RequestBody AppointmentDTO dto,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        if (principal != null && isPatientRole(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Usa /api/appointments/book para agendar tu propia cita.");
        }
        planLimits.checkAppointmentLimit(); // 402 si se superó la cuota mensual del plan
        return service.create(dto);
    }

    /**
     * Auto-agenda de citas para el paciente autenticado.
     *
     * <p><b>Seguridad:</b> el {@code patientId} se toma <em>siempre</em> del JWT,
     * nunca del cuerpo de la petición. Así el paciente solo puede agendar para
     * sí mismo aunque envíe un ID diferente.</p>
     *
     * <p>La cita se crea con estado {@code PENDIENTE}. El especialista o admin
     * la confirmará desde el panel y el paciente recibirá un email de confirmación.</p>
     */
    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDTO book(
            @RequestBody AppointmentDTO dto,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticación requerida.");
        }

        // Forzar siempre el patientId del JWT — ignorar lo que venga en el body
        dto.setPatientId(principal.getUser().getId());
        dto.setStatus("PENDIENTE");

        planLimits.checkAppointmentLimit(); // 402 si se superó la cuota mensual del plan
        return service.create(dto);
    }

    // ── Modificación ──────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public AppointmentDTO update(@PathVariable Long id, @RequestBody AppointmentDTO dto) {
        return service.update(id, dto);
    }

    /**
     * Cambia el estado de una cita. Dispara email al paciente si el estado
     * es {@code CONFIRMADA} o {@code CANCELADA}.
     *
     * <p>Body: {@code { "status": "CONFIRMADA", "cancellationReason": "..." }}</p>
     */
    @PatchMapping("/{id}/status")
    public AppointmentDTO updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return service.updateStatus(id, body.get("status"), body.get("cancellationReason"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean isPatientRole(UserDetailsImpl p) {
        String role = p.getUser().getRole() != null ? p.getUser().getRole().name() : "";
        return "USER".equalsIgnoreCase(role) || "PATIENT".equalsIgnoreCase(role);
    }
}
