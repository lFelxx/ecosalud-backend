package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.multitenancy.TenantContext;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.ServiceRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.AppointmentService;
import com.demo.ecosalud.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository repo;
    private final UserRepository        userRepo;
    private final ServiceRepository     serviceRepo;
    private final EmailService          emailService;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy, HH:mm", java.util.Locale.forLanguageTag("es"));

    @Override
    public List<AppointmentDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getByPatientId(Long patientId) {
        return repo.findByPatientIdOrderByAppointmentDateDesc(patientId)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AppointmentDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    public AppointmentDTO create(AppointmentDTO dto) {
        Appointment a = new Appointment();
        copy(dto, a);
        a.setCreatedAt(LocalDateTime.now());
        if (a.getStatus() == null || a.getStatus().isBlank()) a.setStatus("PENDIENTE");
        return toDTO(repo.save(a));
    }

    @Override
    public AppointmentDTO update(Long id, AppointmentDTO dto) {
        Appointment a = findOrThrow(id);
        copy(dto, a);
        a.setUpdatedAt(LocalDateTime.now());
        return toDTO(repo.save(a));
    }

    @Override
    public AppointmentDTO updateStatus(Long id, String status, String cancellationReason) {
        Appointment a = findOrThrow(id);
        a.setStatus(status);
        if (cancellationReason != null) a.setCancellationReason(cancellationReason);
        a.setUpdatedAt(LocalDateTime.now());
        AppointmentDTO saved = toDTO(repo.save(a));
        sendStatusEmail(a, saved, status);
        return saved;
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Cita no encontrada: " + id);
        repo.deleteById(id);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void sendStatusEmail(Appointment a, AppointmentDTO dto, String status) {
        try {
            String toEmail    = dto.getPatientEmail();
            String patientName = dto.getPatientName();
            String serviceName = dto.getServiceName() != null ? dto.getServiceName() : "la terapia";
            String clinicName  = TenantContext.get();
            String dateTime    = (a.getAppointmentDate() != null && a.getAppointmentTime() != null)
                    ? a.getAppointmentDate().atTime(a.getAppointmentTime()).format(DATE_FMT)
                    : "fecha a confirmar";

            if (toEmail == null || toEmail.isBlank()) {
                log.warn("[EmailService] Sin email para el paciente de la cita {}", a.getId());
                return;
            }

            if ("CONFIRMADA".equalsIgnoreCase(status)) {
                String specialistName = a.getSpecialistId() != null
                        ? userRepo.findById(a.getSpecialistId())
                                  .map(u -> u.getName())
                                  .orElse("el especialista")
                        : "el especialista";
                emailService.sendAppointmentConfirmation(
                        toEmail, patientName, serviceName, dateTime, specialistName, clinicName);
            } else if ("CANCELADA".equalsIgnoreCase(status)) {
                emailService.sendAppointmentCancellation(
                        toEmail, patientName, serviceName, dateTime, a.getCancellationReason());
            }
        } catch (Exception e) {
            log.error("[EmailService] Error al enviar notificación para cita {}: {}", a.getId(), e.getMessage());
        }
    }

    private Appointment findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada: " + id));
    }

    private void copy(AppointmentDTO dto, Appointment a) {
        if (dto.getPatientId()        != null) a.setPatientId(dto.getPatientId());
        if (dto.getSpecialistId()     != null) a.setSpecialistId(dto.getSpecialistId());
        if (dto.getServiceId()        != null) a.setServiceId(dto.getServiceId());
        if (dto.getAppointmentDate()  != null) a.setAppointmentDate(dto.getAppointmentDate());
        if (dto.getAppointmentTime()  != null) a.setAppointmentTime(dto.getAppointmentTime());
        if (dto.getStatus()           != null) a.setStatus(dto.getStatus());
        if (dto.getNotes()            != null) a.setNotes(dto.getNotes());
        if (dto.getCancellationReason()!= null) a.setCancellationReason(dto.getCancellationReason());
    }

    private AppointmentDTO toDTO(Appointment a) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(a.getId());
        dto.setPatientId(a.getPatientId());
        dto.setSpecialistId(a.getSpecialistId());
        dto.setServiceId(a.getServiceId());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setAppointmentTime(a.getAppointmentTime());
        dto.setStatus(a.getStatus());
        dto.setNotes(a.getNotes());
        dto.setCancellationReason(a.getCancellationReason());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());

        // Resolver nombres desde repositorios del tenant activo
        userRepo.findById(a.getPatientId()).ifPresent(u -> {
            dto.setPatientName(u.getName());
            dto.setPatientEmail(u.getEmail());
        });
        if (a.getServiceId() != null) {
            serviceRepo.findById(a.getServiceId())
                    .ifPresent(s -> dto.setServiceName(s.getName()));
        }
        return dto;
    }
}
