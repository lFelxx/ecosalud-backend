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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E: flujo completo del ciclo de vida de un servicio del catálogo.
 * Login → Crear → Consultar → Actualizar → Toggle disponibilidad → Eliminar.
 */
@SpringBootTest
@ActiveProfiles("test")
class CatalogFlowE2ETest {

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
        String email = "e2e_catalog_" + suffix + "@test.com";

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("name", "CatalogTester " + suffix, "email", email, "password", "pass123"))));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "pass123"))))
                .andReturn();

        token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void flujoCompleto_catalogCicloDeVida() throws Exception {
        String nombreServicio = "Servicio E2E " + UUID.randomUUID();

        // Paso 1: Crear servicio
        MvcResult createResult = mockMvc.perform(post("/api/catalog")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", nombreServicio,
                        "description", "Descripción completa del servicio",
                        "price", 80.0,
                        "duration", 60,
                        "availability", true,
                        "category", "Bienestar",
                        "benefits", List.of("Reduce estrés", "Mejora movilidad")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.availability").value(true))
                .andReturn();

        int catalogId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asInt();

        // Paso 2: Consultar el servicio creado
        mockMvc.perform(get("/api/catalog/" + catalogId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(catalogId))
                .andExpect(jsonPath("$.name").value(nombreServicio));

        // Paso 3: Actualizar precio y nombre
        String nombreActualizado = "Servicio Actualizado " + UUID.randomUUID();
        mockMvc.perform(put("/api/catalog/" + catalogId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", nombreActualizado,
                        "description", "Descripción actualizada",
                        "price", 95.0,
                        "duration", 60,
                        "category", "Bienestar"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(95.0))
                .andExpect(jsonPath("$.name").value(nombreActualizado));

        // Paso 4: Toggle disponibilidad (activo → inactivo)
        mockMvc.perform(put("/api/catalog/" + catalogId + "/toggle-availability")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availability").value(false));

        // Paso 5: Eliminar el servicio
        mockMvc.perform(delete("/api/catalog/" + catalogId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Paso 6: Verificar que el servicio ya no existe
        mockMvc.perform(get("/api/catalog/" + catalogId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
