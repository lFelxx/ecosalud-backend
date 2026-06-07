package com.demo.ecosalud.service.impl;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.dto.ResetAdminPasswordRequest;
import com.demo.ecosalud.model.dto.TenantDTO;
import com.demo.ecosalud.model.dto.TenantStatsDTO;
import com.demo.ecosalud.model.dto.UpdateTenantRequest;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.multitenancy.SchemaInitializationService;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.repository.UserRepository;
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

    private final TenantRepository            tenantRepository;
    private final SchemaInitializationService schemaService;
    private final UserRepository              userRepository;
    private final PasswordEncoder             passwordEncoder;
    private final DataSource                  dataSource;

    /** Dominio base para subdominios de tenants — configurable en application.properties */
    @Value("${platform.domain:ecosalud.com}")
    private String platformDomain;

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
        // Fundadores y demos → EXEMPT (sin cobro).
        // Todos los demás → TRIAL: 14 días gratuitos antes de requerir suscripción.
        BillingStatus billing = (accountType == AccountType.FOUNDER || accountType == AccountType.DEMO)
                ? BillingStatus.EXEMPT
                : BillingStatus.TRIAL;

        // Generar nombre de schema PostgreSQL — snake_case con prefijo "tenant_"
        // Ejemplo: slug "dra-angelica-camacho" → schema "tenant_dra_angelica_camacho"
        String schemaName = "tenant_" + req.getSlug().replace("-", "_");

        // Subdominio automático — usa el dominio de plataforma configurado
        // Ejemplo: "dra-angelica-camacho.ecosalud.com"
        String subdomain  = req.getSlug() + "." + platformDomain;

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

    // ── Nuevos métodos de soporte ─────────────────────────────────────────────

    @Override
    public TenantDTO updateTenant(Long id, UpdateTenantRequest req) {
        Tenant t = findOrThrow(id);

        if (req.getName()         != null) t.setName(req.getName());
        if (req.getSpecialty()    != null) t.setSpecialty(req.getSpecialty());
        if (req.getOwnerName()    != null) t.setOwnerName(req.getOwnerName());
        if (req.getPhone()        != null) t.setPhone(req.getPhone());
        if (req.getAddress()      != null) t.setAddress(req.getAddress());
        if (req.getCity()         != null) t.setCity(req.getCity());
        if (req.getCountry()      != null) t.setCountry(req.getCountry());
        if (req.getPrimaryColor() != null) t.setPrimaryColor(req.getPrimaryColor());
        if (req.getLogoUrl()      != null) t.setLogoUrl(req.getLogoUrl());
        if (req.getCustomDomain() != null) t.setCustomDomain(req.getCustomDomain());
        if (req.getPromoCode()    != null) t.setPromoCode(req.getPromoCode());

        // ownerEmail: si cambia, invalidar domainVerified (el nuevo email podría ser diferente)
        if (req.getOwnerEmail() != null && !req.getOwnerEmail().equals(t.getOwnerEmail())) {
            t.setOwnerEmail(req.getOwnerEmail());
        }

        log.info("[SuperAdmin] Tenant '{}' actualizado.", t.getSlug());
        return toDTO(tenantRepository.save(t));
    }

    @Override
    public void resetAdminPassword(Long id, ResetAdminPasswordRequest req) {
        Tenant t = findOrThrow(id);

        // El admin de la clínica vive en public.users, identificado por ownerEmail
        User admin = userRepository.findByEmail(t.getOwnerEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario administrador con email: " + t.getOwnerEmail()));

        admin.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(admin);
        log.info("[SuperAdmin] Contraseña del admin '{}' restablecida por soporte.", t.getOwnerEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public TenantStatsDTO getTenantStats(Long id) {
        Tenant t = findOrThrow(id);
        String s = t.getSchemaName();

        // Usamos JDBC directo con nombre de schema explícito para evitar conflictos de tenant context
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        long users          = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".users");
        long appointments   = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".appointments");
        long pendingApts    = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".appointments WHERE status = 'PENDIENTE'");
        long specialists    = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".specialists WHERE active = true");
        long services       = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".services WHERE active = true");
        long therapyPlans   = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".therapy_plans");
        long healthRecords  = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".health_records");
        long posts          = countSafe(jdbc, "SELECT COUNT(*) FROM " + s + ".posts");

        return TenantStatsDTO.builder()
                .totalUsers(users)
                .totalAppointments(appointments)
                .pendingAppointments(pendingApts)
                .totalSpecialists(specialists)
                .totalServices(services)
                .totalTherapyPlans(therapyPlans)
                .totalHealthRecords(healthRecords)
                .totalPosts(posts)
                .build();
    }

    @Override
    public void deleteTenant(Long id) {
        Tenant t = findOrThrow(id);

        // 1. Eliminar schema PostgreSQL (irreversible)
        schemaService.dropSchema(t.getSchemaName());

        // 2. Eliminar el usuario admin de public.users
        userRepository.findByEmail(t.getOwnerEmail()).ifPresent(userRepository::delete);

        // 3. Eliminar el registro del tenant
        tenantRepository.delete(t);

        log.warn("[SuperAdmin] Tenant '{}' (id={}) eliminado permanentemente.", t.getSlug(), id);
    }

    /** Ejecuta un COUNT(*) y retorna 0 si la tabla no existe o hay error. */
    private long countSafe(JdbcTemplate jdbc, String sql) {
        try {
            Long result = jdbc.queryForObject(sql, Long.class);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.debug("[Stats] No se pudo contar: {} — {}", sql, e.getMessage());
            return 0L;
        }
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
