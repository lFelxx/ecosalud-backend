package com.demo.ecosalud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.demo.ecosalud.config.PayUConfig;

/**
 * Punto de entrada de la aplicación Ecosalud Backend.
 *
 * <p>Variables de entorno requeridas en producción:</p>
 * <ul>
 *   <li>{@code DATABASE_URL}      — URL JDBC de PostgreSQL</li>
 *   <li>{@code DATABASE_USERNAME} — Usuario de la BD</li>
 *   <li>{@code DATABASE_PASSWORD} — Contraseña de la BD</li>
 *   <li>{@code FRONTEND_URL}      — Origen permitido en CORS (URL de Vercel)</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PayUConfig.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
