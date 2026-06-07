package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.HealthRecordDTO;
import com.demo.ecosalud.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para historia clínica electrónica.
 *
 * <p>Ruta base: {@code /api/health-records}</p>
 *
 * <table border="1">
 *   <tr><th>Método</th><th>Ruta</th><th>Descripción</th></tr>
 *   <tr><td>GET</td>    <td>/api/health-records</td>              <td>Lista todas (o por paciente con ?patientId=)</td></tr>
 *   <tr><td>GET</td>    <td>/api/health-records/{id}</td>         <td>Detalle de una historia</td></tr>
 *   <tr><td>POST</td>   <td>/api/health-records</td>              <td>Crear nueva historia clínica</td></tr>
 *   <tr><td>PUT</td>    <td>/api/health-records/{id}</td>         <td>Actualizar (solo si no bloqueada)</td></tr>
 *   <tr><td>PATCH</td>  <td>/api/health-records/{id}/lock</td>    <td>Bloquear historia (inmutable)</td></tr>
 *   <tr><td>DELETE</td> <td>/api/health-records/{id}</td>         <td>Eliminar (solo si no bloqueada)</td></tr>
 * </table>
 *
 * <p>Todos los endpoints requieren JWT válido. El tenant se determina
 * automáticamente a partir del header {@code X-Tenant-Slug}.</p>
 *
 * <p>Cumple con la <strong>Resolución 1995/1999 de MinSalud</strong>:
 * validación de campos obligatorios y bloqueo de registros firmados.</p>
 */
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService service;

    /**
     * Lista historias clínicas del tenant activo.
     * Si se incluye {@code ?patientId=}, filtra por ese paciente.
     */
    @GetMapping
    public List<HealthRecordDTO> list(@RequestParam(required = false) Long patientId) {
        return patientId != null
                ? service.getByPatient(patientId)
                : service.getAll();
    }

    /** Obtiene una historia clínica por su ID. */
    @GetMapping("/{id}")
    public HealthRecordDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /** Crea una nueva historia clínica. Retorna 201 Created con el objeto generado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HealthRecordDTO create(@RequestBody HealthRecordDTO dto) {
        return service.create(dto);
    }

    /**
     * Actualiza una historia clínica existente.
     * Retorna 400 si la historia está bloqueada.
     */
    @PutMapping("/{id}")
    public HealthRecordDTO update(@PathVariable Long id, @RequestBody HealthRecordDTO dto) {
        return service.update(id, dto);
    }

    /**
     * Bloquea una historia clínica.
     * Una vez bloqueada es inmutable: no puede editarse ni eliminarse.
     * Esta acción es irreversible.
     */
    @PatchMapping("/{id}/lock")
    public HealthRecordDTO lock(@PathVariable Long id) {
        return service.lock(id);
    }

    /**
     * Elimina una historia clínica.
     * Retorna 400 si la historia está bloqueada (Res. 1995/1999).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ── Manejo de errores de negocio ──────────────────────────────────────────

    /** Historia bloqueada o campos obligatorios faltantes → 400 Bad Request. */
    @ExceptionHandler({ IllegalStateException.class, IllegalArgumentException.class })
    public ResponseEntity<Map<String, String>> handleBusinessError(RuntimeException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", ex.getMessage()));
    }
}
