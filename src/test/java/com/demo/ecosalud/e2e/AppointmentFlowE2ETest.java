package com.demo.ecosalud.e2e;

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
 * E2E: flujo completo de gestión de citas.
 * Setup → Agendar → Reprogramar → Verificar → Cancelar → Verificar error.
 */
@SpringBootTest
@ActiveProfiles("test")
class AppointmentFlowE2ETest {

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
                        "name", "Paciente E2E " + suffix,
                        "email", "paciente_e2e_" + suffix + "@test.com",
                        "password", "password123"))))
                .andReturn();
        userId = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("id").asInt();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("email", "paciente_e2e_" + suffix + "@test.com", "password", "password123"))))
                .andReturn();
        token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        MvcResult therapistResult = mockMvc.perform(post("/api/therapists/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapeuta E2E " + suffix,
                        "email", "terapeuta_e2e_" + suffix + "@test.com",
                        "password", "password123",
                        "specialty", "Psicología"))))
                .andReturn();
        therapistId = objectMapper.readTree(therapistResult.getResponse().getContentAsString()).get("id").asInt();

        MvcResult catalogResult = mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Terapia E2E " + suffix,
                        "description", "Sesión de prueba",
                        "price", 70.0,
                        "duration", 55,
                        "availability", true,
                        "category", "Psicología",
                        "benefits", List.of("Mejora el bienestar")))))
                .andReturn();
        catalogId = objectMapper.readTree(catalogResult.getResponse().getContentAsString()).get("id").asInt();
    }

    @Test
    void flujoCompleto_agendarReprogramarCancelar() throws Exception {
        // Paso 1: Agendar cita
        MvcResult createResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "therapistId", therapistId,
                        "catalogId", catalogId,
                        "date", LocalDateTime.now().plusDays(10).withNano(0).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"))
                .andExpect(jsonPath("$.userName").isNotEmpty())
                .andExpect(jsonPath("$.therapistName").isNotEmpty())
                .andExpect(jsonPath("$.catalogName").isNotEmpty())
                .andReturn();

        int citaId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // Paso 2: Reprogramar la cita
        mockMvc.perform(put("/api/appointments/" + citaId + "/reschedule")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("newDate", LocalDateTime.now().plusDays(20).withNano(0).toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROGRAMADA"));

        // Paso 3: Verificar estado de la cita
        mockMvc.perform(get("/api/appointments/" + citaId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROGRAMADA"));

        // Paso 4: Cancelar la cita
        mockMvc.perform(put("/api/appointments/" + citaId + "/cancel")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        // Paso 5: Intentar cancelar de nuevo → regla de negocio
        mockMvc.perform(put("/api/appointments/" + citaId + "/cancel")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(jsonPath("$.error").value("Unprocessable Content"));
    }

    @Test
    void consultarCitasPorUsuario_retornaHistorial() throws Exception {
        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", userId,
                        "therapistId", therapistId,
                        "catalogId", catalogId,
                        "date", LocalDateTime.now().plusDays(15).withNano(0).toString()))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/appointments/user/" + userId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(userId));
    }
}
