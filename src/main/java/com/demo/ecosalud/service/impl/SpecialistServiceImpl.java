package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.model.dto.SpecialistDTO;
import com.demo.ecosalud.model.entities.Specialist;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.SpecialistRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.SpecialistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SpecialistServiceImpl implements SpecialistService {

    private final SpecialistRepository repo;
    private final UserRepository       userRepo;

    @Override
    public SpecialistDTO get() {
        return repo.findFirstByActiveTrueOrderByIdAsc()
                .map(this::toDTO)
                .orElse(new SpecialistDTO());
    }

    @Override
    public SpecialistDTO upsert(SpecialistDTO dto) {
        Specialist sp = repo.findFirstByActiveTrueOrderByIdAsc().orElseGet(() -> {
            // Si no hay especialista, crear uno vinculado al primer usuario del tenant
            Specialist nuevo = new Specialist();
            nuevo.setActive(true);
            nuevo.setCreatedAt(LocalDateTime.now());
            // Usar el primer usuario existente como owner del perfil
            userRepo.findAll().stream().findFirst()
                    .ifPresentOrElse(
                            u -> nuevo.setUserId(u.getId()),
                            () -> nuevo.setUserId(1L)
                    );
            return nuevo;
        });

        // Actualizar campos del especialista
        if (dto.getSpecialty()          != null) sp.setSpecialty(dto.getSpecialty());
        if (dto.getRegistrationNumber() != null) sp.setRegistrationNumber(dto.getRegistrationNumber());
        if (dto.getBio()                != null) sp.setBio(dto.getBio());
        if (dto.getPhotoUrl()           != null) sp.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getUniversity()         != null) sp.setUniversity(dto.getUniversity());
        if (dto.getBadge()              != null) sp.setBadge(dto.getBadge());

        sp = repo.save(sp);

        // Si se envió un nombre, actualizar el nombre del usuario vinculado
        if (dto.getName() != null && !dto.getName().isBlank()) {
            userRepo.findById(sp.getUserId()).ifPresent(u -> {
                u.setName(dto.getName());
                userRepo.save(u);
            });
        }

        return toDTO(sp);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private SpecialistDTO toDTO(Specialist sp) {
        SpecialistDTO dto = new SpecialistDTO();
        dto.setId(sp.getId());
        dto.setUserId(sp.getUserId());
        dto.setSpecialty(sp.getSpecialty());
        dto.setRegistrationNumber(sp.getRegistrationNumber());
        dto.setBio(sp.getBio());
        dto.setPhotoUrl(sp.getPhotoUrl());
        dto.setUniversity(sp.getUniversity());
        dto.setBadge(sp.getBadge());
        dto.setActive(sp.isActive());

        // Resolver el nombre del usuario vinculado
        Optional<User> user = userRepo.findById(sp.getUserId());
        dto.setName(user.map(User::getName).orElse(""));

        return dto;
    }
}
