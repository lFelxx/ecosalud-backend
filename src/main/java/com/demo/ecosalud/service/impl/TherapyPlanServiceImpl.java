package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.TherapyPlanDTO;
import com.demo.ecosalud.model.entities.TherapyPlan;
import com.demo.ecosalud.repository.ServiceRepository;
import com.demo.ecosalud.repository.TherapyPlanRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.TherapyPlanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TherapyPlanServiceImpl implements TherapyPlanService {

    private final TherapyPlanRepository repo;
    private final UserRepository        userRepo;
    private final ServiceRepository     serviceRepo;

    @Override
    public List<TherapyPlanDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TherapyPlanDTO> getByPatientId(Long patientId) {
        return repo.findByPatientIdOrderByCreatedAtDesc(patientId)
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TherapyPlanDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    public TherapyPlanDTO create(TherapyPlanDTO dto) {
        TherapyPlan p = new TherapyPlan();
        copy(dto, p);
        p.setCreatedAt(LocalDateTime.now());
        if (p.getStatus() == null || p.getStatus().isBlank()) p.setStatus("ACTIVO");
        return toDTO(repo.save(p));
    }

    @Override
    public TherapyPlanDTO update(Long id, TherapyPlanDTO dto) {
        TherapyPlan p = findOrThrow(id);
        copy(dto, p);
        return toDTO(repo.save(p));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Plan no encontrado: " + id);
        repo.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private TherapyPlan findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan de terapia no encontrado: " + id));
    }

    private void copy(TherapyPlanDTO dto, TherapyPlan p) {
        if (dto.getPatientId()    != null) p.setPatientId(dto.getPatientId());
        if (dto.getSpecialistId() != null) p.setSpecialistId(dto.getSpecialistId());
        if (dto.getServiceId()    != null) p.setServiceId(dto.getServiceId());
        if (dto.getStartDate()    != null) p.setStartDate(dto.getStartDate());
        if (dto.getStatus()       != null) p.setStatus(dto.getStatus());
        if (dto.getNotes()        != null) p.setNotes(dto.getNotes());
        p.setTotalSessions(dto.getTotalSessions() > 0 ? dto.getTotalSessions() : p.getTotalSessions());
        p.setCompletedSessions(Math.max(0, dto.getCompletedSessions()));
    }

    private TherapyPlanDTO toDTO(TherapyPlan p) {
        TherapyPlanDTO dto = new TherapyPlanDTO();
        dto.setId(p.getId());
        dto.setPatientId(p.getPatientId());
        dto.setSpecialistId(p.getSpecialistId());
        dto.setServiceId(p.getServiceId());
        dto.setTotalSessions(p.getTotalSessions());
        dto.setCompletedSessions(p.getCompletedSessions());
        dto.setStartDate(p.getStartDate());
        dto.setStatus(p.getStatus());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());

        // Resolver nombres (null-safe: patient_id puede ser null en datos legados)
        if (p.getPatientId() != null) {
            userRepo.findById(p.getPatientId()).ifPresent(u -> {
                dto.setPatientName(u.getName());
                dto.setPatientEmail(u.getEmail());
            });
        }
        if (p.getServiceId() != null) {
            serviceRepo.findById(p.getServiceId())
                    .ifPresent(s -> dto.setServiceName(s.getName()));
        }
        return dto;
    }
}
