package com.demo.ecosalud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.demo.ecosalud.multitenancy.TenantFilter;
import com.demo.ecosalud.service.impl.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

/**
 * Configuración central de seguridad de la plataforma Ecosalud Market.
 *
 * <ul>
 *   <li>CORS dinámico (FRONTEND_URL o localhost:5173 por defecto)</li>
 *   <li>JWT stateless + TenantFilter para multi-tenancy</li>
 *   <li>Rutas públicas: /api/auth/**, /api/user/register, /api/tenant/*</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter              jwtFilter;
    private final TenantFilter           tenantFilter;
    private final UserDetailsServiceImpl userDetailsService;

    /** Orígenes permitidos en CORS — configurable via FRONTEND_URL en producción. */
    @Value("${frontend.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Autenticación y registro público
                        .requestMatchers("/auth/**", "/api/auth/**", "/api/user/register").permitAll()
                        // Onboarding público — registro de nuevas clínicas sin JWT
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/onboarding").permitAll()
                        // Landing pública de cada tenant (sin JWT)
                        .requestMatchers("/api/tenant/**").permitAll()
                        // Endpoints públicos del sitio de la clínica (sin JWT)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/services/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/published").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/specialist").permitAll()
                        // Imágenes públicas — servidas sin JWT para embeberse en landing pages
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/media/files/**").permitAll()
                        // Info pública de la clínica (para JSON-LD / SEO)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/public/**").permitAll()
                        // Health check — usado por Railway/Render para saber que el backend arrancó
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/health").permitAll()
                        // FHIR CapabilityStatement — público por especificación FHIR R4
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/fhir/metadata").permitAll()
                        // Webhook de PayU — debe ser público (PayU llama sin JWT)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/billing/payu/confirmation").permitAll()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                // TenantFilter debe ejecutarse ANTES del JwtFilter
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** BCrypt con factor de trabajo 10. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of(
				"https://ecosalud-frontend-hvxn.vercel.app",
				"http://localhost:5173",
				"http://localhost:3000"
		));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
