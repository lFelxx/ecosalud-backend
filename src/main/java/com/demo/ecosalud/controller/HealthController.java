package com.demo.ecosalud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Endpoint de salud del sistema.
 *
 * <p>Utilizado por Railway / Render como healthcheck para determinar
 * que el contenedor arrancó correctamente y está listo para recibir tráfico.
 * También útil para monitoreo externo (UptimeRobot, BetterStack, etc.).</p>
 *
 * <p>Ruta: {@code GET /api/health} — pública, sin JWT.</p>
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status",    "UP",
                "service",   "ecosalud-backend",
                "timestamp", Instant.now().toString()
        ));
    }
}
