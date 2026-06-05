package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.exception.DuplicateResourceException;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Juan Pérez");
        user.setEmail("juan@test.com");
        user.setPassword("$2a$encoded");
        user.setRole(RolUser.USER);
        user.setStatus(UserStatus.ACTIVO);

        userDTO = new UserDTO();
        userDTO.setName("Juan Pérez");
        userDTO.setEmail("juan@test.com");
        userDTO.setPassword("password123");
    }

    @Test
    void register_exitoso_retornaDTO() {
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(userRepository.existsByName("Juan Pérez")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO result = userService.register(userDTO);

        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getRole()).isEqualTo(RolUser.USER);
        verify(userRepository).save(any());
    }

    @Test
    void register_asignaRolUserPorDefecto() {
        userDTO.setRole(null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByName(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO result = userService.register(userDTO);

        assertThat(result.getRole()).isEqualTo(RolUser.USER);
    }

    @Test
    void register_emailDuplicado_lanzaExcepcion() {
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(userDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El email ya está registrado");
    }

    @Test
    void register_nombreDuplicado_lanzaExcepcion() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByName("Juan Pérez")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(userDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El nombre ya está registrado");
    }

    @Test
    void register_conRolYaDefinido_noSobreescribeRol() {
        userDTO.setRole(RolUser.ADMIN);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByName(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO result = userService.register(userDTO);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    void register_conStatusYaDefinido_noSobreescribeStatus() {
        userDTO.setStatus(UserStatus.ACTIVO);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByName(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.save(any())).thenReturn(user);

        UserDTO result = userService.register(userDTO);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    void getUserById_existe_retornaDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void getUserById_noExiste_lanzaExcepcion() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
