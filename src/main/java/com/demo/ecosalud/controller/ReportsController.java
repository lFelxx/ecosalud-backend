package com.demo.ecosalud.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecosalud.model.dto.ReportsSummaryDTO;
import com.demo.ecosalud.model.dto.ReportsSummaryDTO.ServiceStatDTO;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.HealthRecordRepository;
import com.demo.ecosalud.repository.ServiceRepository;
import com.demo.ecosalud.repository.TherapyPlanRepository;
import com.demo.ecosalud.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reportes y estadísticas del tenant activo.
 *
 * <p>Ruta base: {@code /api/reports}</p>
 * <p>Solo accesible para usuarios autenticados (ADMIN o EDITOR).</p>
 *
 * <p>Todos los datos se calculan en tiempo real sobre las tablas del schema
 * del tenant activo (TenantContext resuelto por TenantFilter).</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final AppointmentRepository  appointmentRepo;
    private final TherapyPlanRepository  therapyPlanRepo;
    private final HealthRecordRepository healthRecordRepo;
    private final ServiceRepository      serviceRepo;
    private final UserRepository         userRepo;

    /**
     * Resumen estadístico de la clínica.
     *
     * <p>Incluye: totales de citas por estado, pacientes, servicios más
     * solicitados, planes de terapia activos e historia clínica.</p>
     */
    @GetMapping("/summary")
    public ReportsSummaryDTO summary() {

        // ── Citas ─────────────────────────────────────────────────────────────
        List<Appointment> all = appointmentRepo.findAll();
        long total      = all.size();
        long pending    = count(all, "PENDIENTE");
        long confirmed  = count(all, "CONFIRMADA");
        long completed  = count(all, "COMPLETADA");
        long cancelled  = count(all, "CANCELADA");

        // ── Top servicios ─────────────────────────────────────────────────────
        // Agrupa las citas por serviceId y resuelve el nombre del servicio
        Map<Long, Long> serviceCounts = all.stream()
                .filter(a -> a.getServiceId() != null)
                .collect(Collectors.groupingBy(Appointment::getServiceId, Collectors.counting()));

        List<ServiceStatDTO> topServices = serviceCounts.entrySet().stream()
                .map(e -> {
                    String name = serviceRepo.findById(e.getKey())
                            .map(s -> s.getName())
                            .orElse("Servicio #" + e.getKey());
                    int pct = total > 0 ? (int) Math.round(e.getValue() * 100.0 / total) : 0;
                    return ServiceStatDTO.builder()
                            .serviceName(name)
                            .count(e.getValue())
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparingLong(ServiceStatDTO::getCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // ── Otras métricas ────────────────────────────────────────────────────
        long totalPatients    = userRepo.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equalsIgnoreCase("USER"))
                .count();
        long totalServices    = serviceRepo.count();
        long totalPlans       = therapyPlanRepo.count();
        long activePlans      = therapyPlanRepo.findByStatus("ACTIVO").size();
        long totalHCE         = healthRecordRepo.count();
        int  occupancy        = total > 0 ? (int) Math.round(completed * 100.0 / total) : 0;

        log.debug("[Reports] Summary generado: {} citas, {} pacientes, {} planes", total, totalPatients, totalPlans);

        return ReportsSummaryDTO.builder()
                .totalAppointments(total)
                .pendingAppointments(pending)
                .confirmedAppointments(confirmed)
                .completedAppointments(completed)
                .cancelledAppointments(cancelled)
                .totalPatients(totalPatients)
                .totalServices(totalServices)
                .topServices(topServices)
                .totalTherapyPlans(totalPlans)
                .activeTherapyPlans(activePlans)
                .totalHealthRecords(totalHCE)
                .occupancyRate(occupancy)
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private long count(List<Appointment> list, String status) {
        return list.stream().filter(a -> status.equalsIgnoreCase(a.getStatus())).count();
    }
}
