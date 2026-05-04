package com.demo.ecosalud.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.demo.ecosalud.model.dto.LoginRequestDTO;
import com.demo.ecosalud.model.dto.LoginResponseDTO;
import com.demo.ecosalud.service.AuthService;
import com.demo.ecosalud.util.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDTO.getEmail(),
                        requestDTO.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        String token = jwtUtils.generateToken(userDetails);

        return new LoginResponseDTO(token, userDetails.getUsername(), userDetails.getUser().getRole().name());
    }

}
