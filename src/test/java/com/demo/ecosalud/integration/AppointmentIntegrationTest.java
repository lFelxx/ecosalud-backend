package com.demo.ecosalud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración: gestión de citas — Controller → Service → Repository → H2.
 */
@SpringBootTest
@ActiveProfiles("test")
class AppointmentIntegrationTest {

    @Autowired WebApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;
    private int userId;
    private int therapistId;
    private int catalogId;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);

        MvcResult userResult = mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Paciente Int " + suffix,
                        "email", "paciente_int_" + suffix + "@test.com",
                        "password", "password123"))))
                .andReturn();
        userId = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("id").asInt();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", "paciente_int_" + suffix + "@test.com", "password", "password123"))))
                .andReturn();
        token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        MvcResult therapistResult = mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapeuta Int " + suffix,
                        "email", "terapeuta_int_" + suffix + "@test.com",
                        "password", "password123",
                        "specialty", "Psicología"))))
                .andReturn();
        therapistId = objectMapper.readTree(therapistResult.getResponse().getContentAsString()).get("id").asInt();

        MvcResult catalogResult = mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Servicio Int " + suffix,
                        "description", "Servicio de prueba",
                        "price", 60.0,
                        "duration", 50,
                        "availability", true,
                        "category", "Salud",
                        "benefits", List.of("Beneficio 1")))))
                .andReturn();
        catalogId = objectMapper.readTree(catalogResult.getResponse().getContentAsString()).get("id").asInt();
    }

    @Test
    void agendarCita_datosValidos_retornaCitaConfirmada() throws Exception {
        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "therapistId", therapistId,
                        "catalogId", catalogId,
                        "date", LocalDateTime.now().plusDays(7).withNano(0).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void cancelarCita_citaExistente_retornaCancelada() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "therapistId", therapistId,
                        "catalogId", catalogId,
                        "date", LocalDateTime.now().plusDays(8).withNano(0).toString()))))
                .andReturn();

        int citaId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(put("/api/appointments/" + citaId + "/cancel")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void agendarCita_conflictoHorarioPaciente_retornaError() throws Exception {
        LocalDateTime mismaFecha = LocalDateTime.now().plusDays(9).withNano(0);

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "therapistId", therapistId,
                        "catalogId", catalogId,
                        "date", mismaFecha.toString()))));

        String suffix2 = UUID.randomUUID().toString().substring(0, 8);
        MvcResult otroTerapeutaResult = mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapeuta2 Int " + suffix2,
                        "email", "terapeuta2_int_" + suffix2 + "@test.com",
                        "password", "password123",
                        "specialty", "Fisioterapia"))))
                .andReturn();
        int otroTerapeutaId = objectMapper.readTree(otroTerapeutaResult.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "therapistId", otroTerapeutaId,
                        "catalogId", catalogId,
                        "date", mismaFecha.toString()))))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"));
    }
}
