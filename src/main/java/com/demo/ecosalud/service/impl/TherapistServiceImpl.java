package com.demo.ecosalud.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.mapper.TherapistMapper;
import com.demo.ecosalud.model.dto.TherapistDTO;
import com.demo.ecosalud.model.dto.TherapistRegisterDTO;
import com.demo.ecosalud.model.entities.Therapist;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.TherapistRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.TherapistService;

/**
 * Implementación del servicio de gestión de perfiles de terapeutas.
 */
@RequiredArgsConstructor
@Service
@Transactional
public class TherapistServiceImpl implements TherapistService {

    private final TherapistRepository therapistRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registro unificado: crea el User con rol THERAPIST y su perfil profesional
     * en una sola transacción. Los datos del perfil (especialidad, etc.) son opcionales.
     */
    @Override
    public TherapistDTO registerTherapist(TherapistRegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (userRepository.existsByName(dto.getName())) {
            throw new RuntimeException("El nombre ya está registrado");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(RolUser.THERAPIST);
        user.setStatus(UserStatus.ACTIVO);
        User savedUser = userRepository.save(user);

        Therapist therapist = new Therapist();
        therapist.setUser(savedUser);
        therapist.setSpecialty(dto.getSpecialty());
        therapist.setBiography(dto.getBiography());
        therapist.setYearsOfExperience(dto.getYearsOfExperience());
        therapist.setAvailable(true);
        Therapist saved = therapistRepository.save(therapist);
        return TherapistMapper.toDTO(saved);
    }

    /**
     * Obtiene el perfil de un terapeuta por su ID de perfil.
     */
    @Override
    public TherapistDTO getTherapistById(Long id) {
        return therapistRepository.findById(id)
                .map(TherapistMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Terapeuta no encontrado con id: " + id));
    }

    /**
     * Obtiene el perfil de un terapeuta a partir del ID de su usuario.
     * Útil cuando el frontend tiene el userId del usuario autenticado.
     */
    @Override
    public TherapistDTO getTherapistByUserId(Long userId) {
        return therapistRepository.findByUserId(userId)
                .map(TherapistMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe perfil de terapeuta para el usuario con id: " + userId));
    }

    /**
     * Retorna todos los terapeutas registrados (uso administrativo).
     */
    @Override
    public List<TherapistDTO> getAllTherapists() {
        return therapistRepository.findAll()
                .stream()
                .map(TherapistMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna solo los terapeutas disponibles para nuevas citas.
     */
    @Override
    public List<TherapistDTO> getAvailableTherapists() {
        return therapistRepository.findByAvailable(true)
                .stream()
                .map(TherapistMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna los terapeutas de una especialidad específica.
     */
    @Override
    public List<TherapistDTO> getTherapistsBySpecialty(String specialty) {
        return therapistRepository.findBySpecialtyContainingIgnoreCase(specialty)
                .stream()
                .map(TherapistMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza los datos del perfil profesional. No permite cambiar el usuario asociado.
     *
     * @param id           ID del perfil a actualizar
     * @param therapistDTO nuevos datos del perfil
     * @return perfil actualizado
     */
    @Override
    public TherapistDTO updateTherapist(Long id, TherapistDTO therapistDTO) {
        Therapist therapist = therapistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Terapeuta no encontrado con id: " + id));

        therapist.setSpecialty(therapistDTO.getSpecialty());
        therapist.setBiography(therapistDTO.getBiography());
        therapist.setYearsOfExperience(therapistDTO.getYearsOfExperience());

        if (therapistDTO.getAvailable() != null) {
            therapist.setAvailable(therapistDTO.getAvailable());
        }

        Therapist saved = therapistRepository.save(therapist);
        return TherapistMapper.toDTO(saved);
    }

    /**
     * Invierte la disponibilidad del terapeuta: disponible → no disponible o viceversa.
     * Útil para que el terapeuta active/desactive su agenda rápidamente.
     */
    @Override
    public TherapistDTO toggleAvailability(Long id) {
        Therapist therapist = therapistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Terapeuta no encontrado con id: " + id));

        therapist.setAvailable(!therapist.getAvailable());
        Therapist saved = therapistRepository.save(therapist);
        return TherapistMapper.toDTO(saved);
    }

    /**
     * Elimina permanentemente el perfil de un terapeuta.
     */
    @Override
    public void deleteTherapist(Long id) {
        if (!therapistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Terapeuta no encontrado con id: " + id);
        }
        therapistRepository.deleteById(id);
    }
}
