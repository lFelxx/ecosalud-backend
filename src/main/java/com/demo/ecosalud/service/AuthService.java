package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.LoginRequestDTO;
import com.demo.ecosalud.model.dto.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO requestDTO);

}
