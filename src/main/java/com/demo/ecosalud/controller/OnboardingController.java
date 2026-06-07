package com.demo.ecosalud.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.dto.OnboardingRequest;
import com.demo.ecosalud.model.dto.OnboardingResponse;
import com.demo.ecosalud.model.dto.TenantDTO;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.model.entities.TrialUsedEmail;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.repository.TrialUsedEmailRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.EmailService;
import com.demo.ecosalud.service.TenantService;
import com.demo.ecosalud.service.impl.UserDetailsImpl;
import com.demo.ecosalud.util.JwtUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registro público de nuevas clínicas — flujo de onboarding.
 *
 * <p>Ruta base: {@code /api/onboarding}</p>
 * <p>Acceso: público (sin JWT). Ver SecurityConfig.</p>
 *
 * <p>Flujo completo:</p>
 * <ol>
 *   <li>Valida unicidad de slug y email</li>
 *   <li>Crea el tenant y su schema en PostgreSQL</li>
 *   <li>Crea el usuario administrador en {@code public.users}</li>
 *   <li>Envía email de bienvenida con credenciales</li>
 *   <li>Genera y retorna un JWT para auto-login inmediato</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final TenantService           tenantService;
    private final TenantRepository        tenantRepository;
    private final UserRepository          userRepository;
    private final TrialUsedEmailRepository trialUsedEmailRepo;
    private final PasswordEncoder         passwordEncoder;
    private final JwtUtils                jwtUtils;
    private final EmailService            emailService;

    /** Caracteres seguros para contraseñas temporales (sin ambiguos: 0, O, l, 1, I). */
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#";
    private static final SecureRandom RNG = new SecureRandom();

    // ── POST /api/onboarding ─────────────────────────────────────────────────

    /**
     * Registra una nueva clínica y retorna un JWT para auto-login.
     *
     * @param req datos del formulario de onboarding
     * @return token JWT + datos del usuario admin + subdominio
     * @throws IllegalArgumentException si el slug o email ya están en uso (→ 409)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingResponse registrar(@Valid @RequestBody OnboardingRequest req) {

        String normalizedEmail = req.getOwnerEmail().toLowerCase().trim();

        // 1. Validar unicidad y blacklist de trial
        if (tenantRepository.existsBySlug(req.getSlug())) {
            throw new IllegalArgumentException("Ya existe una clínica con la URL: " + req.getSlug());
        }
        if (tenantRepository.existsByOwnerEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Ya existe una clínica con el email: " + normalizedEmail);
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("El email ya tiene una cuenta registrada: " + normalizedEmail);
        }
        // Prevenir re-uso del trial gratuito con el mismo email
        if (trialUsedEmailRepo.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                "Este email ya fue utilizado para una prueba gratuita. " +
                "Para continuar usando la plataforma, adquiere un plan en /precios o contáctanos.");
        }

        // 2. Crear tenant
        CreateTenantRequest tenantReq = new CreateTenantRequest();
        tenantReq.setName(req.getClinicName());
        tenantReq.setSlug(req.getSlug());
        tenantReq.setOwnerName(req.getOwnerName());
        tenantReq.setOwnerEmail(normalizedEmail);
        tenantReq.setPhone(req.getPhone());
        tenantReq.setSpecialty(req.getSpecialty());
        tenantReq.setCity(req.getCity());
        tenantReq.setCountry(req.getCountry() != null ? req.getCountry() : "CO");
        tenantReq.setPrimaryColor("#3DAA96");
        tenantReq.setAccountType(AccountType.REGULAR);

        // Parsear plan — default STARTER si viene vacío o inválido
        try {
            tenantReq.setPlan(SubscriptionPlan.valueOf(req.getPlan().toUpperCase()));
        } catch (Exception e) {
            tenantReq.setPlan(SubscriptionPlan.STARTER);
        }

        TenantDTO tenant = tenantService.createTenant(tenantReq);

        // 3. Generar contraseña temporal segura (10 chars)
        String rawPassword = generarPassword(10);

        // 4. Crear usuario administrador en public.users
        User admin = new User();
        admin.setName(req.getOwnerName());
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(RolUser.ADMIN);
        admin.setStatus(UserStatus.ACTIVO);
        admin.setCreatedAt(LocalDateTime.now());
        admin = userRepository.save(admin);

        // Registrar email en blacklist de trials para prevenir re-uso
        trialUsedEmailRepo.save(TrialUsedEmail.builder()
            .email(normalizedEmail)
            .tenantSlug(tenant.getSlug())
            .build());

        log.info("[Onboarding] Clínica '{}' registrada. Admin: {} (id={})", tenant.getSlug(), admin.getEmail(), admin.getId());

        // 5. Enviar email de bienvenida con credenciales
        try {
            String loginUrl = "https://" + tenant.getSubdomain() + "/login";
            emailService.sendWelcomeCredentials(
                    admin.getEmail(),
                    admin.getName(),
                    rawPassword,
                    tenant.getName(),
                    loginUrl
            );
        } catch (Exception e) {
            // El email no es crítico — continuamos aunque falle
            log.warn("[Onboarding] No se pudo enviar email de bienvenida a {}: {}", admin.getEmail(), e.getMessage());
        }

        // 6. Generar JWT para auto-login
        UserDetailsImpl userDetails = new UserDetailsImpl(admin);
        String token = jwtUtils.generateToken(userDetails);

        return OnboardingResponse.builder()
                .token(token)
                .userId(admin.getId())
                .userName(admin.getName())
                .userEmail(admin.getEmail())
                .role(admin.getRole().name())
                .subdomain(tenant.getSubdomain())
                .clinicName(tenant.getName())
                .build();
    }

    // ── Utilidades ──────────────────────────────────────────────────────────

    /** Genera una contraseña temporal aleatoria de {@code length} caracteres. */
    private static String generarPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(RNG.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
