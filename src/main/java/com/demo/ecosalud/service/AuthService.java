package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.LoginRequestDTO;
import com.demo.ecosalud.model.dto.LoginResponseDTO;

/**
 * Contrato para el servicio de autenticación.
 */
public interface AuthService {

    /**
     * Autentica un usuario y genera un token JWT.
     *
     * @param requestDTO credenciales de acceso
     * @return token JWT junto con email y rol del usuario
     */
    LoginResponseDTO login(LoginRequestDTO requestDTO);

}
