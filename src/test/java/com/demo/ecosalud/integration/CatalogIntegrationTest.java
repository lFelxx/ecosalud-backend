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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración: CRUD del catálogo — Controller → Service → Repository → H2.
 */
@SpringBootTest
@ActiveProfiles("test")
class CatalogIntegrationTest {

    @Autowired WebApplicationContext context;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "catalog_int_" + suffix + "@test.com";

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("name", "CatalogTester " + suffix, "email", email, "password", "password123"))));

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "password123"))))
                .andReturn();

        token = objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void crearCatalog_datosValidos_retorna200ConId() throws Exception {
        mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCatalog("Servicio-" + UUID.randomUUID()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.availability").value(true));
    }

    @Test
    void obtenerCatalogPorId_existente_retornaDatos() throws Exception {
        String nombre = "Servicio-" + UUID.randomUUID();
        MvcResult created = mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCatalog(nombre))))
                .andReturn();

        int id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(get("/api/catalog/" + id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(nombre));
    }

    @Test
    void actualizarCatalog_precioNuevo_retornaActualizado() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCatalog("Original-" + UUID.randomUUID()))))
                .andReturn();

        int id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asInt();
        String nombreActualizado = "Actualizado-" + UUID.randomUUID();

        mockMvc.perform(put("/api/catalog/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", nombreActualizado,
                        "description", "Nueva descripción",
                        "price", 120.0,
                        "duration", 45,
                        "category", "Bienestar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(120.0))
                .andExpect(jsonPath("$.name").value(nombreActualizado));
    }

    @Test
    void eliminarCatalog_existente_noApareceEnConsulta() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCatalog("Eliminar-" + UUID.randomUUID()))))
                .andReturn();

        int id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(delete("/api/catalog/" + id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/catalog/" + id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    private Map<String, Object> buildCatalog(String nombre) {
        return Map.of(
                "name", nombre,
                "description", "Descripción de " + nombre,
                "price", 75.0,
                "duration", 45,
                "availability", true,
                "category", "Bienestar",
                "benefits", List.of("Mejora la salud"));
    }
}
