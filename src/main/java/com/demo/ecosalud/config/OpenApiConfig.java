package com.demo.ecosalud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI para la documentación interactiva de la API.
 * Acceso: <a href="http://localhost:8080/swagger-ui.html">swagger-ui.html</a>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecosalud API")
                        .description("API REST para la plataforma de salud Ecosalud. " +
                                "Gestiona usuarios, terapeutas, catálogo de servicios y citas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Ecosalud")
                                .email("andresssh999@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT obtenido en el endpoint de login")));
    }
}
