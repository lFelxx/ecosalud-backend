package com.demo.ecosalud.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E: flujo completo de autenticación.
 * Registro → Login → Acceso a recurso protegido con token.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthFlowE2ETest {

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
    void flujoCompleto_registroLoginAccesoProtegido() throws Exception {
        String email = "e2e_auth_" + UUID.randomUUID() + "@test.com";
        String nombre = "E2E Auth " + UUID.randomUUID();

        // Paso 1: Registrar usuario
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("name", nombre, "email", email, "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // Paso 2: Login y obtención del token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", email, "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Paso 3: Acceder a recurso protegido con el token
        mockMvc.perform(get("/api/catalog")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void loginConCredencialesInvalidas_retorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", "noexiste@test.com", "password", "wrongpassword"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void accesoSinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isUnauthorized());
    }
}
