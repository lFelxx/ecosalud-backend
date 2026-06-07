package com.demo.ecosalud.controller;

import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.model.dto.PlatformPublicDTO;
import com.demo.ecosalud.model.dto.PlatformPublicDTO.FeaturedClinic;
import com.demo.ecosalud.model.dto.PublicClinicInfoDTO;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.multitenancy.TenantContext;
import com.demo.ecosalud.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * Endpoints completamente públicos (sin JWT) con información de la clínica.
 *
 * <p>Ruta base: {@code /api/public}</p>
 *
 * <p>El tenant activo se resuelve vía {@code X-Tenant-Slug} header que
 * el frontend envía en todas las peticiones sin JWT (ver axiosClient.ts).</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final TenantRepository tenantRepo;

    /** Estados de billing que representan clínicas "usando la plataforma" (para el carrusel). */
    private static final Set<BillingStatus> VISIBLE_STATUSES =
        Set.of(BillingStatus.ACTIVE, BillingStatus.EXEMPT, BillingStatus.TRIAL);

    /**
     * Devuelve métricas públicas de la plataforma y perfiles de clínicas para el carrusel
     * de la landing page de marketing.
     *
     * <p>Endpoint <strong>público</strong> — no requiere JWT ni X-Tenant-Slug.</p>
     */
    @GetMapping("/platform")
    public PlatformPublicDTO getPlatformInfo() {
        List<Tenant> all = tenantRepo.findAll().stream()
            // null se trata como activo (compatibilidad con tenants creados antes de la columna)
            .filter(t -> !Boolean.FALSE.equals(t.getActive())
                      && VISIBLE_STATUSES.contains(t.getBillingStatus()))
            .toList();

        long trialCount = all.stream()
            .filter(t -> t.getBillingStatus() == BillingStatus.TRIAL)
            .count();

        List<FeaturedClinic> featured = all.stream()
            .filter(t -> t.getOwnerName() != null && t.getSpecialty() != null)
            .map(t -> FeaturedClinic.builder()
                .ownerName(t.getOwnerName())
                .specialty(t.getSpecialty())
                .clinicName(t.getName())
                .city(t.getCity())
                .logoUrl(t.getLogoUrl())
                .plan(t.getPlan() != null ? t.getPlan().name() : "STARTER")
                .build())
            .toList();

        return PlatformPublicDTO.builder()
            .totalClinics(all.size())
            .trialClinics((int) trialCount)
            .featuredClinics(featured)
            .build();
    }

    /**
     * Devuelve información pública básica de la clínica activa.
     *
     * <p>Usada por el frontend para construir el JSON-LD schema.org
     * (MedicalClinic / LocalBusiness) para SEO.</p>
     *
     * <p>Si el schema no corresponde a ningún tenant registrado (ej. dev local
     * sin header), devuelve un objeto mínimo con valores por defecto.</p>
     */
    @GetMapping("/info")
    public PublicClinicInfoDTO getPublicInfo() {
        String schema = TenantContext.get();
        log.debug("[PublicController] /info — schema activo: {}", schema);

        return tenantRepo.findBySchemaName(schema)
                .map(t -> PublicClinicInfoDTO.builder()
                        .clinicName(t.getName())
                        .specialty(t.getSpecialty())
                        .phone(t.getPhone())
                        .address(t.getAddress())
                        .city(t.getCity())
                        .country(t.getCountry() != null ? t.getCountry() : "CO")
                        .subdomain(t.getSubdomain())
                        .primaryColor(t.getPrimaryColor() != null ? t.getPrimaryColor() : "#3DAA96")
                        .build())
                .orElseGet(() -> {
                    log.debug("[PublicController] tenant no encontrado para schema '{}' — usando defaults", schema);
                    return PublicClinicInfoDTO.builder()
                            .clinicName("Ecosalud")
                            .specialty("Medicina Integrativa")
                            .country("CO")
                            .primaryColor("#3DAA96")
                            .build();
                });
    }
}
