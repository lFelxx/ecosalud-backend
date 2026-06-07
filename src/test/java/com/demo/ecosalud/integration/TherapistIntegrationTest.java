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
class TherapistIntegrationTest {

    @Autowired WebApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;
    private int therapistId;
    private int therapistUserId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        // Registrar usuario normal para obtener token
        String userEmail = "user_th_" + suffix + "@test.com";
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Usuario Th " + suffix,
                        "email", userEmail,
                        "password", "password123"))));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", userEmail, "password", "password123"))))
                .andReturn();
        token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Registrar terapeuta
        MvcResult therapistResult = mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapeuta It " + suffix,
                        "email", "terapeuta_it_" + suffix + "@test.com",
                        "password", "password123",
                        "specialty", "Psicología",
                        "biography", "Especialista con experiencia",
                        "yearsOfExperience", 5))))
                .andReturn();

        var therapistNode = objectMapper.readTree(therapistResult.getResponse().getContentAsString());
        therapistId = therapistNode.get("id").asInt();
        therapistUserId = therapistNode.get("userId").asInt();
    }

    @Test
    void registrarTerapeuta_datosValidos_retornaPerfilCreado() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Nuevo Terapeuta " + suffix,
                        "email", "nuevo_th_" + suffix + "@test.com",
                        "password", "password123",
                        "specialty", "Nutrición"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.specialty").value("Nutrición"));
    }

    @Test
    void registrarTerapeuta_emailDuplicado_retorna409() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "dup_th_" + suffix + "@test.com";

        mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapeuta Dup " + suffix,
                        "email", email,
                        "password", "password123",
                        "specialty", "Psicología"))));

        mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapeuta Dup2 " + suffix,
                        "email", email,
                        "password", "password123",
                        "specialty", "Nutrición"))))
                .andExpect(status().isConflict());
    }

    @Test
    void obtenerTerapeutaPorId_existente_retornaDatos() throws Exception {
        mockMvc.perform(get("/api/therapists/" + therapistId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(therapistId));
    }

    @Test
    void obtenerTerapeutaPorId_noExistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/therapists/999999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerTerapeutaPorUserId_existente_retornaDatos() throws Exception {
        mockMvc.perform(get("/api/therapists/user/" + therapistUserId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(therapistUserId));
    }

    @Test
    void listarTodosTerapeutas_autenticado_retornaLista() throws Exception {
        mockMvc.perform(get("/api/therapists")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listarTerapeutasDisponibles_autenticado_retornaLista() throws Exception {
        mockMvc.perform(get("/api/therapists/available")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buscarPorEspecialidad_autenticado_retornaLista() throws Exception {
        mockMvc.perform(get("/api/therapists/specialty/Psicología")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void actualizarTerapeuta_datosValidos_retornaPerfil() throws Exception {
        mockMvc.perform(put("/api/therapists/" + therapistId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "specialty", "Psicología Clínica",
                        "biography", "Actualizado",
                        "yearsOfExperience", 10,
                        "available", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Psicología Clínica"));
    }

    @Test
    void toggleDisponibilidad_terapeutaExistente_cambia() throws Exception {
        mockMvc.perform(put("/api/therapists/" + therapistId + "/toggle-availability")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").exists());
    }

    @Test
    void eliminarTerapeuta_existente_retorna200() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        MvcResult result = mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Para Borrar " + suffix,
                        "email", "borrar_th_" + suffix + "@test.com",
                        "password", "password123",
                        "specialty", "Nutrición"))))
                .andReturn();

        int id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(delete("/api/therapists/" + id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/therapists"))
                .andExpect(status().isUnauthorized());
    }
}
