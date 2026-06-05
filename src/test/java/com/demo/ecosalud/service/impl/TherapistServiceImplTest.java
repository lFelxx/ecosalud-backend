package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.exception.DuplicateResourceException;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.TherapistDTO;
import com.demo.ecosalud.model.dto.TherapistRegisterDTO;
import com.demo.ecosalud.model.entities.Therapist;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.TherapistRepository;
import com.demo.ecosalud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TherapistServiceImplTest {

    @Mock private TherapistRepository therapistRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TherapistServiceImpl therapistService;

    private User user;
    private Therapist therapist;
    private TherapistRegisterDTO registerDTO;
    private TherapistDTO therapistDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Dr. García");
        user.setEmail("garcia@test.com");
        user.setRole(RolUser.THERAPIST);
        user.setStatus(UserStatus.ACTIVO);

        therapist = new Therapist();
        therapist.setId(1L);
        therapist.setUser(user);
        therapist.setSpecialty("Psicología");
        therapist.setBiography("Especialista con 10 años de experiencia");
        therapist.setYearsOfExperience(10);
        therapist.setAvailable(true);

        registerDTO = new TherapistRegisterDTO();
        registerDTO.setName("Dr. García");
        registerDTO.setEmail("garcia@test.com");
        registerDTO.setPassword("password123");
        registerDTO.setSpecialty("Psicología");
        registerDTO.setBiography("Especialista con 10 años de experiencia");
        registerDTO.setYearsOfExperience(10);

        therapistDTO = new TherapistDTO();
        therapistDTO.setSpecialty("Fisioterapia");
        therapistDTO.setBiography("Fisioterapeuta certificado");
        therapistDTO.setYearsOfExperience(5);
        therapistDTO.setAvailable(true);
    }

    @Test
    void registerTherapist_exitoso_retornaDTO() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByName(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(therapistRepository.save(any())).thenReturn(therapist);

        TherapistDTO result = therapistService.registerTherapist(registerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getSpecialty()).isEqualTo("Psicología");
        verify(userRepository).save(any());
        verify(therapistRepository).save(any());
    }

    @Test
    void registerTherapist_emailExistente_lanzaExcepcion() {
        when(userRepository.existsByEmail("garcia@test.com")).thenReturn(true);

        assertThatThrownBy(() -> therapistService.registerTherapist(registerDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El email ya está registrado");
    }

    @Test
    void registerTherapist_nombreExistente_lanzaExcepcion() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByName("Dr. García")).thenReturn(true);

        assertThatThrownBy(() -> therapistService.registerTherapist(registerDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El nombre ya está registrado");
    }

    @Test
    void getTherapistById_existe_retornaDTO() {
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));

        TherapistDTO result = therapistService.getTherapistById(1L);

        assertThat(result.getSpecialty()).isEqualTo("Psicología");
    }

    @Test
    void getTherapistById_noExiste_lanzaExcepcion() {
        when(therapistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> therapistService.getTherapistById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTherapistByUserId_existe_retornaDTO() {
        when(therapistRepository.findByUserId(1L)).thenReturn(Optional.of(therapist));

        TherapistDTO result = therapistService.getTherapistByUserId(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getTherapistByUserId_noExiste_lanzaExcepcion() {
        when(therapistRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> therapistService.getTherapistByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTherapists_retornaLista() {
        when(therapistRepository.findAll()).thenReturn(List.of(therapist));

        List<TherapistDTO> results = therapistService.getAllTherapists();

        assertThat(results).hasSize(1);
    }

    @Test
    void getAvailableTherapists_retorlaDisponibles() {
        when(therapistRepository.findByAvailable(true)).thenReturn(List.of(therapist));

        List<TherapistDTO> results = therapistService.getAvailableTherapists();

        assertThat(results).hasSize(1);
    }

    @Test
    void getTherapistsBySpecialty_retornaFiltrados() {
        when(therapistRepository.findBySpecialtyContainingIgnoreCase("Psico"))
                .thenReturn(List.of(therapist));

        List<TherapistDTO> results = therapistService.getTherapistsBySpecialty("Psico");

        assertThat(results).hasSize(1);
    }

    @Test
    void updateTherapist_exitoso_retornaActualizado() {
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(therapistRepository.save(any())).thenReturn(therapist);

        TherapistDTO result = therapistService.updateTherapist(1L, therapistDTO);

        assertThat(result).isNotNull();
        verify(therapistRepository).save(any());
    }

    @Test
    void updateTherapist_noExiste_lanzaExcepcion() {
        when(therapistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> therapistService.updateTherapist(99L, therapistDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTherapist_availableNulo_noActualizaDisponibilidad() {
        therapistDTO.setAvailable(null);
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(therapistRepository.save(any())).thenReturn(therapist);

        TherapistDTO result = therapistService.updateTherapist(1L, therapistDTO);

        assertThat(result).isNotNull();
        verify(therapistRepository).save(any());
    }

    @Test
    void toggleAvailability_disponibleLoDesactiva() {
        therapist.setAvailable(true);
        Therapist desactivado = new Therapist();
        desactivado.setId(1L);
        desactivado.setUser(user);
        desactivado.setAvailable(false);

        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(therapistRepository.save(any())).thenReturn(desactivado);

        TherapistDTO result = therapistService.toggleAvailability(1L);

        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void deleteTherapist_existe_elimina() {
        when(therapistRepository.existsById(1L)).thenReturn(true);

        therapistService.deleteTherapist(1L);

        verify(therapistRepository).deleteById(1L);
    }

    @Test
    void deleteTherapist_noExiste_lanzaExcepcion() {
        when(therapistRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> therapistService.deleteTherapist(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
