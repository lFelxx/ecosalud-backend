package com.demo.ecosalud.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración central de seguridad de la aplicación.
 *
 * <p>Gestiona CORS, autorización HTTP y política de sesiones.</p>
 * <p>CORS acepta el origen definido en {@code FRONTEND_URL} (variable de entorno)
 * o {@code localhost:5173} por defecto para desarrollo local.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** URL del frontend (Vercel en prod, localhost en dev). */
    @Value("${frontend.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Define las reglas HTTP: CORS, CSRF deshabilitado (API REST stateless),
     * rutas públicas y protegidas.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // El registro es público; el resto requiere autenticación
                .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    /**
     * Fuente de configuración CORS registrada para todas las rutas {@code /**}.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Soporta múltiples orígenes separados por coma
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Encoder BCrypt (factor de trabajo 10) para contraseñas. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
