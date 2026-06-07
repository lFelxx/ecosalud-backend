package com.demo.ecosalud.controller;

import java.security.SecureRandom;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.demo.ecosalud.model.dto.LoginRequestDTO;
import com.demo.ecosalud.model.dto.LoginResponseDTO;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.AuthService;
import com.demo.ecosalud.service.EmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Autenticación pública — login y recuperación de contraseña.
 * Ruta base: {@code /api/auth}
 */
@Slf4j
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService     authService;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService    emailService;

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#";
    private static final SecureRandom RNG = new SecureRandom();

    /** Inicio de sesión — retorna JWT. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Recuperación de contraseña — genera una nueva contraseña temporal,
     * la guarda con BCrypt y la envía por email.
     *
     * <p>Siempre responde 204 (incluso si el email no existe) para no
     * revelar qué usuarios están registrados.</p>
     */
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        if (email.isBlank()) return;

        userRepository.findByEmail(email).ifPresent(user -> {
            String newRaw = generatePassword(10);
            user.setPassword(passwordEncoder.encode(newRaw));
            userRepository.save(user);

            try {
                // Reutiliza la plantilla de bienvenida con la nueva contraseña
                emailService.sendWelcomeCredentials(
                        user.getEmail(),
                        user.getName(),
                        newRaw,
                        "Ecosalud",
                        "https://ecosalud.com/login"
                );
            } catch (Exception e) {
                log.warn("[Auth] No se pudo enviar email de contraseña a {}: {}", email, e.getMessage());
            }

            log.info("[Auth] Contraseña temporal generada para: {}", email);
        });
    }

    private static String generatePassword(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(PASSWORD_CHARS.charAt(RNG.nextInt(PASSWORD_CHARS.length())));
        return sb.toString();
    }
}
