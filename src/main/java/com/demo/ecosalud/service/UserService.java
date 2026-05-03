package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.UserDTO;

public interface UserService {
    
    UserDTO register(UserDTO userDTO);
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
}
