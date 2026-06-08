package com.demo.ecosalud.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.multitenancy.SchemaInitializationService;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.service.TenantService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializa los dos tenants fundadores de la plataforma al arrancar la app.
 *
 * <p>Solo se ejecuta si los tenants no existen aún en la base de datos.
 * Es idempotente — se puede reiniciar la app sin duplicar tenants.</p>
 *
 * <p><strong>Tenants fundadores (Plan CLINIC ilimitado — EXENTO de pago):</strong></p>
 * <ul>
 *   <li>dra-angelica-camacho — Dra. Angélica Camacho (Terapias alternativas y farmacología vegetal)</li>
 *   <li>fisiosalud           — Fisiosalud SAS (IPS habilitada, 10+ especialistas)</li>
 * </ul>
 *
 * <p><strong>NOTA DE DOMINIO:</strong> Los slugs usan guiones (no puntos) para
 * cumplir con el estándar DNS de subdominios. El punto en "Dra." es solo display.</p>
 *
 * <p><strong>Credenciales iniciales — CAMBIAR EN PRODUCCIÓN:</strong></p>
 * <pre>
 *   ── SUPER ADMIN (Plataforma) ──────────────────────────────────────────
 *     Propietarios : Ing. Félix Castro · Ing. Elkin Chaparro
 *     Email        : superadmin@ecosaludmarket.com
 *     Password     : EcoSaaS#2024
 *     URL          : admin.ecosaludmarket.com
 *
 *   ── TENANT 1: Dra. Angélica Camacho ──────────────────────────────────
 *     Email        : admin@dra-angelica-camacho.ecosalud.com
 *     Password     : Angelica#Clinic2024
 *     URL inicial  : dra-angelica-camacho.ecosalud.com
 *     URL futura   : www.dra-angelica-camacho.com.co
 *     Especialidad : Terapias alternativas y farmacología vegetal
 *
 *   ── TENANT 2: Fisiosalud SAS ──────────────────────────────────────────
 *     Email        : admin@fisiosalud.ecosalud.com
 *     Password     : Fisio#Clinic2024
 *     URL inicial  : fisiosalud.ecosalud.com
 *     URL futura   : www.fisiosalud.com.co
 *     Especialidades: Fisioterapia · Fonoaudiología · Psicología
 *                     Terapia Ocupacional · Rehabilitación
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FounderTenantsSeeder implements ApplicationRunner {

    private final TenantRepository          tenantRepository;
    private final TenantService             tenantService;
    private final SchemaInitializationService schemaInitializationService;

    @Override
    public void run(ApplicationArguments args) {
        seedFounderAngelicaCamacho();
        seedFounderFisiosalud();
    }

    // ── Tenant 1: Dra. Angélica Camacho ─────────────────────────────────────

    private void seedFounderAngelicaCamacho() {
        // Slug DNS-seguro: sin puntos (DNS no admite puntos en labels de subdominio)
        // Display: "Dra. Angélica Camacho" — URL: dra-angelica-camacho.ecosalud.com
        final String slug        = "dra-angelica-camacho";
        final String schemaName  = "tenant_" + slug.replace("-", "_");

        if (tenantRepository.existsBySlug(slug)) {
            log.info("[Seeder] Tenant '{}' ya existe — aplicando migración de tablas.", slug);
            schemaInitializationService.migrateSchema(schemaName);
            // Parchar campos que pueden ser null en registros creados antes de esta versión
            tenantRepository.findBySlug(slug).ifPresent(t -> patchFounderFields(t,
                "Dra. Angélica Camacho",
                "Terapias alternativas y farmacología vegetal",
                "Bogotá", "Dra. Angélica Camacho"));
            return;
        }

        CreateTenantRequest req = new CreateTenantRequest();
        req.setName("Dra. Angélica Camacho");
        req.setSlug(slug);
        req.setOwnerName("Dra. Angélica Camacho");
        req.setOwnerEmail("admin@dra-angelica-camacho.ecosalud.com");
        req.setPhone("+57 300 000 0001");
        req.setCity("Bogotá");
        req.setCountry("CO");
        // Especialidad oficial del tenant fundador #1
        req.setSpecialty("Terapias alternativas y farmacología vegetal");
        req.setPrimaryColor("#3DAA96");
        req.setPlan(SubscriptionPlan.FOUNDER);
        req.setAccountType(AccountType.FOUNDER);

        try {
            tenantService.createTenant(req);
            log.info("[Seeder] ✅ Tenant fundador '{}' creado.", slug);
        } catch (Exception e) {
            log.error("[Seeder] Error al crear tenant '{}': {}", slug, e.getMessage());
        }
    }

    // ── Helper — actualiza campos de carrusel si estaban null ────────────────

    /**
     * Idempotente: solo escribe si el campo es null en BD.
     * Garantiza que ownerName, specialty y city nunca estén vacíos
     * para que el tenant sea visible en el carrusel de la landing page.
     */
    private void patchFounderFields(Tenant t, String name,
                                     String specialty, String city, String ownerName) {
        boolean dirty = false;
        if (t.getOwnerName() == null || t.getOwnerName().isBlank()) {
            t.setOwnerName(ownerName); dirty = true;
        }
        if (t.getSpecialty() == null || t.getSpecialty().isBlank()) {
            t.setSpecialty(specialty); dirty = true;
        }
        if (t.getCity() == null || t.getCity().isBlank()) {
            t.setCity(city); dirty = true;
        }
        if (t.getName() == null || t.getName().isBlank()) {
            t.setName(name); dirty = true;
        }
        // Garantizar que el tenant esté activo (null = inactivo en versiones antiguas)
        if (!Boolean.TRUE.equals(t.getActive())) {
            t.setActive(true); dirty = true;
        }
        if (dirty) {
            tenantRepository.save(t);
            log.info("[Seeder] Tenant '{}' — campos de carrusel actualizados.", t.getSlug());
        }
    }

    // ── Tenant 2: Fisiosalud SAS ─────────────────────────────────────────────

    private void seedFounderFisiosalud() {
        final String slug       = "fisiosalud";
        final String schemaName = "tenant_" + slug.replace("-", "_");

        if (tenantRepository.existsBySlug(slug)) {
            log.info("[Seeder] Tenant '{}' ya existe — aplicando migración de tablas.", slug);
            schemaInitializationService.migrateSchema(schemaName);
            // Parchar campos que pueden ser null en registros creados antes de esta versión
            tenantRepository.findBySlug(slug).ifPresent(t -> patchFounderFields(t,
                "Fisiosalud SAS",
                "Fisioterapia · Fonoaudiología · Psicología · Terapia Ocupacional",
                "Colombia", "Administrador Fisiosalud SAS"));
            return;
        }

        CreateTenantRequest req = new CreateTenantRequest();
        req.setName("Fisiosalud SAS");
        req.setSlug(slug);
        req.setOwnerName("Administrador Fisiosalud SAS");
        // Email admin del tenant fundador #2 — actualizar con email real en producción
        req.setOwnerEmail("admin@fisiosalud.ecosalud.com");
        req.setPhone("+57 300 000 0002");
        req.setCity("Colombia");
        req.setCountry("CO");
        // IPS con 20+ años de trayectoria — múltiples especialidades
        req.setSpecialty("Fisioterapia · Fonoaudiología · Psicología · Terapia Ocupacional · Rehabilitación");
        // Color corporativo de Fisiosalud — azul IPS (diferenciado del verde de Ecosalud)
        req.setPrimaryColor("#1A5F8A");
        req.setPlan(SubscriptionPlan.FOUNDER);
        req.setAccountType(AccountType.FOUNDER);

        try {
            tenantService.createTenant(req);
            log.info("[Seeder] ✅ Tenant fundador '{}' creado.", slug);
        } catch (Exception e) {
            log.error("[Seeder] Error al crear tenant '{}': {}", slug, e.getMessage());
        }
    }
}
