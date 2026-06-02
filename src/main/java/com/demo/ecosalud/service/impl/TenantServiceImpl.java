package com.demo.ecosalud.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.dto.TenantDTO;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.multitenancy.SchemaInitializationService;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.service.TenantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación del servicio de tenants.
 *
 * <p>Orquesta la creación de tenants: persiste en {@code public.tenants}
 * y delega la inicialización del schema al {@link SchemaInitializationService}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository    tenantRepository;
    private final SchemaInitializationService schemaService;

    @Override
    public TenantDTO createTenant(CreateTenantRequest req) {
        // Validaciones de unicidad
        if (tenantRepository.existsBySlug(req.getSlug())) {
            throw new IllegalArgumentException("Ya existe una clínica con el slug: " + req.getSlug());
        }
        if (tenantRepository.existsByOwnerEmail(req.getOwnerEmail())) {
            throw new IllegalArgumentException("Ya existe una clínica con el email: " + req.getOwnerEmail());
        }

        // Determinar plan y estado de facturación
        SubscriptionPlan plan = req.getPlan() != null ? req.getPlan() : SubscriptionPlan.STARTER;
        AccountType accountType = req.getAccountType() != null ? req.getAccountType() : AccountType.REGULAR;
        BillingStatus billing = (accountType == AccountType.FOUNDER || accountType == AccountType.DEMO)
                ? BillingStatus.EXEMPT
                : BillingStatus.ACTIVE;

        // Generar nombre de schema (snake_case, prefijo tenant_)
        String schemaName = "tenant_" + req.getSlug().replace("-", "_");
        String subdomain  = req.getSlug() + ".ecosaludmarket.com";

        Tenant tenant = Tenant.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .schemaName(schemaName)
                .subdomain(subdomain)
                .ownerName(req.getOwnerName())
                .ownerEmail(req.getOwnerEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .city(req.getCity())
                .country(req.getCountry() != null ? req.getCountry() : "CO")
                .specialty(req.getSpecialty())
                .primaryColor(req.getPrimaryColor() != null ? req.getPrimaryColor() : "#3DAA96")
                .plan(plan)
                .accountType(accountType)
                .billingStatus(billing)
                .promoCode(req.getPromoCode())
                .active(true)
                .schemaInitialized(false)
                .build();

        tenant = tenantRepository.save(tenant);

        // Crear schema y tablas en PostgreSQL
        try {
            schemaService.initializeSchema(schemaName);
            tenant.setSchemaInitialized(true);
            tenant = tenantRepository.save(tenant);
            log.info("Tenant '{}' creado correctamente. Schema: {}", req.getSlug(), schemaName);
        } catch (Exception e) {
            log.error("Error al crear schema para '{}': {}", req.getSlug(), e.getMessage());
            // El tenant queda en BD con schemaInitialized=false para reintento
        }

        return toDTO(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantDTO> getAllTenants() {
        return tenantRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDTO getBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado: " + slug));
    }

    @Override
    public TenantDTO toggleActive(Long id, boolean active) {
        Tenant t = findOrThrow(id);
        t.setActive(active);
        return toDTO(tenantRepository.save(t));
    }

    @Override
    public TenantDTO changePlan(Long id, String planName) {
        Tenant t = findOrThrow(id);
        t.setPlan(SubscriptionPlan.valueOf(planName.toUpperCase()));
        return toDTO(tenantRepository.save(t));
    }

    @Override
    public TenantDTO toggleBillingExemption(Long id, boolean exempt) {
        Tenant t = findOrThrow(id);
        t.setBillingStatus(exempt ? BillingStatus.EXEMPT : BillingStatus.ACTIVE);
        return toDTO(tenantRepository.save(t));
    }

    @Override
    public TenantDTO verifyCustomDomain(Long id, String domain) {
        Tenant t = findOrThrow(id);
        t.setCustomDomain(domain);
        t.setDomainVerified(true);
        return toDTO(tenantRepository.save(t));
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private Tenant findOrThrow(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado con id: " + id));
    }

    /** Convierte entidad a DTO sin exponer datos sensibles de infraestructura. */
    private TenantDTO toDTO(Tenant t) {
        return TenantDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .slug(t.getSlug())
                .subdomain(t.getSubdomain())
                .customDomain(t.getCustomDomain())
                .domainVerified(t.getDomainVerified())
                .ownerName(t.getOwnerName())
                .ownerEmail(t.getOwnerEmail())
                .phone(t.getPhone())
                .city(t.getCity())
                .country(t.getCountry())
                .specialty(t.getSpecialty())
                .primaryColor(t.getPrimaryColor())
                .logoUrl(t.getLogoUrl())
                .plan(t.getPlan())
                .accountType(t.getAccountType())
                .billingStatus(t.getBillingStatus())
                .active(t.getActive())
                .schemaInitialized(t.getSchemaInitialized())
                .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null)
                .build();
    }
}
