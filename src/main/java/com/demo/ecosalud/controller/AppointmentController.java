package com.demo.ecosalud.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.model.dto.RescheduleRequestDTO;
import com.demo.ecosalud.service.AppointmentService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ──────────────────────── CREATE ────────────────────────

    @PostMapping
    public AppointmentDTO scheduleAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        return appointmentService.scheduleAppointment(appointmentDTO);
    }

    // ──────────────────────── READ ──────────────────────────

    @GetMapping("/{id}")
    public AppointmentDTO getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @GetMapping
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/user/{userId}")
    public List<AppointmentDTO> getAppointmentsByUser(@PathVariable Long userId) {
        return appointmentService.getAppointmentsByUser(userId);
    }

    @GetMapping("/status/{status}")
    public List<AppointmentDTO> getAppointmentsByStatus(@PathVariable AppointmentSatus status) {
        return appointmentService.getAppointmentsByStatus(status);
    }

    // ──────────────────────── UPDATE ────────────────────────

    @PutMapping("/{id}/reschedule")
    public AppointmentDTO rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDTO request) {
        return appointmentService.rescheduleAppointment(id, request.getNewDate());
    }

    @PutMapping("/{id}/cancel")
    public AppointmentDTO cancelAppointment(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }

    @PutMapping("/{id}/confirm")
    public AppointmentDTO confirmAppointment(@PathVariable Long id) {
        return appointmentService.confirmAppointment(id);
    }

    // ──────────────────────── DELETE ────────────────────────

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }
}
