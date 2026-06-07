package com.demo.ecosalud.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import jakarta.transaction.Transactional;

import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.mapper.UserMapper;
import com.demo.ecosalud.model.dto.UserDTO;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.UserService;

@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO register(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (userRepository.existsByName(userDTO.getName())) {
            throw new RuntimeException("El nombre ya está registrado");
        }

        User user = UserMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if (user.getRole() == null) {
            user.setRole(RolUser.USER);
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVO);
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        User save = userRepository.save(user);

        return UserMapper.toDTO(save);

    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // Busca el usuario o lanza excepción si no existe
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // Actualiza los campos editables
        usuario.setName(userDTO.getName());
        usuario.setEmail(userDTO.getEmail());
        usuario.setRole(userDTO.getRole());
        usuario.setStatus(userDTO.getStatus());

        // Solo re-hashea la contraseña si se envía una nueva
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        return UserMapper.toDTO(userRepository.save(usuario));
    }

    @Override
    public void deleteUser(Long id) {
        // Verifica existencia antes de eliminar para retornar un error claro
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }

}
