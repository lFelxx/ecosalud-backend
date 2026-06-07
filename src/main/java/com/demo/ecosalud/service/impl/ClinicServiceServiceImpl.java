package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.ServiceDTO;
import com.demo.ecosalud.model.entities.Service;
import com.demo.ecosalud.repository.ServiceRepository;
import com.demo.ecosalud.service.ClinicServiceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional
public class ClinicServiceServiceImpl implements ClinicServiceService {

    private final ServiceRepository repo;

    @Override
    public List<ServiceDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ServiceDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    public ServiceDTO create(ServiceDTO dto) {
        Service s = new Service();
        copy(dto, s);
        s.setCreatedAt(LocalDateTime.now());
        return toDTO(repo.save(s));
    }

    @Override
    public ServiceDTO update(Long id, ServiceDTO dto) {
        Service s = findOrThrow(id);
        copy(dto, s);
        return toDTO(repo.save(s));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Servicio no encontrado: " + id);
        repo.deleteById(id);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Service findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + id));
    }

    private void copy(ServiceDTO dto, Service s) {
        if (dto.getName()           != null) s.setName(dto.getName());
        if (dto.getCategory()       != null) s.setCategory(dto.getCategory());
        if (dto.getDescription()    != null) s.setDescription(dto.getDescription());
        if (dto.getBenefits()       != null) s.setBenefits(dto.getBenefits());
        if (dto.getDurationMinutes()!= null) s.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getPriceCop()       != null) s.setPriceCop(dto.getPriceCop());
        if (dto.getImageUrl()       != null) s.setImageUrl(dto.getImageUrl());
        s.setActive(dto.isActive());
    }

    private ServiceDTO toDTO(Service s) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setCategory(s.getCategory());
        dto.setDescription(s.getDescription());
        dto.setBenefits(s.getBenefits());
        dto.setDurationMinutes(s.getDurationMinutes());
        dto.setPriceCop(s.getPriceCop());
        dto.setImageUrl(s.getImageUrl());
        dto.setActive(s.isActive());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
