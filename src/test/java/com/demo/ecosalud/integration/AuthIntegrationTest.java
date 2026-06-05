package com.demo.ecosalud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración: autenticación — Controller → Service → Repository → H2.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired WebApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void login_credencialesValidas_retornaToken() throws Exception {
        String email = "auth_int_" + UUID.randomUUID() + "@test.com";
        registrarUsuario(email, "Auth Int " + UUID.randomUUID());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_passwordIncorrecta_retorna401() throws Exception {
        String email = "auth_int2_" + UUID.randomUUID() + "@test.com";
        registrarUsuario(email, "Auth Int2 " + UUID.randomUUID());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", email, "password", "incorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void endpointProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registro_emailDuplicado_retorna409() throws Exception {
        String email = "dup_" + UUID.randomUUID() + "@test.com";
        registrarUsuario(email, "Usuario " + UUID.randomUUID());

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("name", "OtroNombre " + UUID.randomUUID(),
                                "email", email,
                                "password", "pass123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    private void registrarUsuario(String email, String nombre) throws Exception {
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("name", nombre, "email", email, "password", "password123"))));
    }
}
