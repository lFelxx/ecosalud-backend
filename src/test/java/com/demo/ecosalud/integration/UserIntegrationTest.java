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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired WebApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;
    private int userId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "user_int_" + suffix + "@test.com";

        MvcResult registerResult = mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Usuario Int " + suffix,
                        "email", email,
                        "password", "password123"))))
                .andReturn();
        userId = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("id").asInt();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andReturn();
        token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void registrarUsuario_datosValidos_retornaUsuarioCreado() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Nuevo Usuario " + suffix,
                        "email", "nuevo_" + suffix + "@test.com",
                        "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("nuevo_" + suffix + "@test.com"));
    }

    @Test
    void obtenerUsuarioPorId_existente_retornaDatos() throws Exception {
        mockMvc.perform(get("/api/user/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    void obtenerUsuarioPorId_noExistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/user/999999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerUsuarioPorId_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/user/" + userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtFilter_tokenMalformado_rechazaPeticion() throws Exception {
        mockMvc.perform(get("/api/user/" + userId)
                .header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtFilter_headerSinBearerPrefix_rechazaPeticion() throws Exception {
        mockMvc.perform(get("/api/user/" + userId)
                .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }
}
