package com.demo.ecosalud.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import jakarta.transaction.Transactional;

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
    public UserDTO register(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (userRepository.existsByName(userDTO.getName())) {
            throw new RuntimeException("El nombre ya está registrado");
        }
        
       User user = UserMapper.toEntity(userDTO);
       user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

       User save = userRepository.save(user);

       return UserMapper.toDTO(save);
    
    }

    @Override
    public UserDTO getUserById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserById'");
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUser(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }
    
}
