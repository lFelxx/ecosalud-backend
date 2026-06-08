package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO de información pública de la plataforma Ecosalud Market.
 *
 * <p>Consumido por la landing page del marketing — no requiere autenticación.
 * Expone métricas agregadas y perfiles de clínicas que usan la plataforma
 * para mostrar prueba social a potenciales clientes.</p>
 *
 * <p><strong>Privacy:</strong> Solo se expone ownerName, specialty, clinicName, city y logoUrl.
 * Nunca email, teléfono ni datos de pacientes.</p>
 */
@Data
@Builder
public class PlatformPublicDTO {

    /** Total de clínicas con acceso activo a la plataforma. */
    private int totalClinics;

    /** Total de tenants en período de prueba (trial). */
    private int trialClinics;

    /** Perfiles públicos de clínicas destacadas para el carrusel de la landing. */
    private List<FeaturedClinic> featuredClinics;

    // ── Sub-DTO ────────────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class FeaturedClinic {
        /** Nombre del profesional de salud / propietario de la clínica. */
        private String ownerName;
        /** Especialidad médica o área de salud. */
        private String specialty;
        /** Nombre comercial de la clínica. */
        private String clinicName;
        /** Ciudad donde opera la clínica. */
        private String city;
        /** URL del logo de la clínica (puede ser null). */
        private String logoUrl;
        /** Plan de suscripción: STARTER | PRO | CLINIC | FOUNDER. */
        private String plan;
    }
}
