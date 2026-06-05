package com.demo.ecosalud.controller;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.model.dto.RescheduleRequestDTO;
import com.demo.ecosalud.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de citas médicas.
 * Base URL: {@code /api/appointments}
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Citas", description = "Agendamiento, reprogramación y cancelación de citas médicas")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Agendar cita", description = "Crea una nueva cita validando disponibilidad del terapeuta y conflictos de horario")
    @ApiResponse(responseCode = "200", description = "Cita agendada en estado CONFIRMADA")
    @ApiResponse(responseCode = "404", description = "Paciente, terapeuta o servicio no encontrado")
    @ApiResponse(responseCode = "422", description = "Regla de negocio violada (conflicto de horario, terapeuta no disponible)")
    @PostMapping
    public AppointmentDTO scheduleAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        return appointmentService.scheduleAppointment(appointmentDTO);
    }

    @Operation(summary = "Obtener cita por ID")
    @ApiResponse(responseCode = "200", description = "Cita encontrada")
    @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    @GetMapping("/{id}")
    public AppointmentDTO getAppointmentById(@Parameter(description = "ID de la cita") @PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @Operation(summary = "Listar todas las citas")
    @GetMapping
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @Operation(summary = "Listar citas de un paciente")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    @GetMapping("/user/{userId}")
    public List<AppointmentDTO> getAppointmentsByUser(
            @Parameter(description = "ID del paciente") @PathVariable Long userId) {
        return appointmentService.getAppointmentsByUser(userId);
    }

    @Operation(summary = "Listar citas por estado")
    @GetMapping("/status/{status}")
    public List<AppointmentDTO> getAppointmentsByStatus(
            @Parameter(description = "Estado de la cita: PENDIENTE, CONFIRMADA, CANCELADA, REPROGRAMADA")
            @PathVariable AppointmentSatus status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    @Operation(summary = "Reprogramar cita", description = "Cambia la fecha de la cita. No aplica a citas canceladas.")
    @ApiResponse(responseCode = "200", description = "Cita reprogramada")
    @ApiResponse(responseCode = "422", description = "Cita cancelada o conflicto en la nueva fecha")
    @PutMapping("/{id}/reschedule")
    public AppointmentDTO rescheduleAppointment(
            @Parameter(description = "ID de la cita") @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDTO request) {
        return appointmentService.rescheduleAppointment(id, request.getNewDate());
    }

    @Operation(summary = "Cancelar cita")
    @ApiResponse(responseCode = "200", description = "Cita cancelada")
    @ApiResponse(responseCode = "422", description = "La cita ya está cancelada")
    @PutMapping("/{id}/cancel")
    public AppointmentDTO cancelAppointment(@Parameter(description = "ID de la cita") @PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }

    @Operation(summary = "Confirmar cita")
    @ApiResponse(responseCode = "200", description = "Cita confirmada")
    @ApiResponse(responseCode = "422", description = "No se puede confirmar una cita cancelada")
    @PutMapping("/{id}/confirm")
    public AppointmentDTO confirmAppointment(@Parameter(description = "ID de la cita") @PathVariable Long id) {
        return appointmentService.confirmAppointment(id);
    }

    @Operation(summary = "Eliminar cita", description = "Eliminación permanente. Solo para administradores.")
    @DeleteMapping("/{id}")
    public void deleteAppointment(@Parameter(description = "ID de la cita") @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }
}
