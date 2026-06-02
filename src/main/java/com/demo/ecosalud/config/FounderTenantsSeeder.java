package com.demo.ecosalud.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.model.dto.CreateTenantRequest;
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
 * <p><strong>Tenants fundadores:</strong></p>
 * <ul>
 *   <li>ecosalud-camacho — Dra. Angélica Camacho (Medicina Integrativa)</li>
 *   <li>fisiosalud       — Fisiosalud SAS (IPS multidisciplinaria)</li>
 * </ul>
 *
 * <p><strong>Credenciales de acceso a la plataforma:</strong></p>
 * <pre>
 *   SUPER ADMIN
 *     Email:    superadmin@ecosaludmarket.com
 *     Password: EcoSaaS#2024  (cambiar en producción)
 *
 *   Dra. Angélica Camacho (Tenant Admin)
 *     Email:    admin@ecosalud-camacho.ecosaludmarket.com
 *     Password: Angelica#Clinic2024  (cambiar al primer login)
 *     URL:      ecosalud-camacho.ecosaludmarket.com
 *
 *   Fisiosalud SAS (Tenant Admin)
 *     Email:    admin@fisiosalud.ecosaludmarket.com
 *     Password: Fisio#Clinic2024  (cambiar al primer login)
 *     URL:      fisiosalud.ecosaludmarket.com
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FounderTenantsSeeder implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final TenantService    tenantService;

    @Override
    public void run(ApplicationArguments args) {
        seedFounderAngelicaCamacho();
        seedFounderFisiosalud();
    }

    // ── Tenant 1: Dra. Angélica Camacho ─────────────────────────────────────

    private void seedFounderAngelicaCamacho() {
        final String slug = "ecosalud-camacho";
        if (tenantRepository.existsBySlug(slug)) {
            log.info("[Seeder] Tenant '{}' ya existe — omitiendo.", slug);
            return;
        }

        CreateTenantRequest req = new CreateTenantRequest();
        req.setName("Ecosalud Camacho — Medicina Integrativa");
        req.setSlug(slug);
        req.setOwnerName("Dra. Angélica Camacho");
        req.setOwnerEmail("admin@ecosalud-camacho.ecosaludmarket.com");
        req.setPhone("+57 300 000 0001");
        req.setCity("Bogotá");
        req.setCountry("CO");
        req.setSpecialty("Medicina Alternativa e Integrativa");
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

    // ── Tenant 2: Fisiosalud SAS ─────────────────────────────────────────────

    private void seedFounderFisiosalud() {
        final String slug = "fisiosalud";
        if (tenantRepository.existsBySlug(slug)) {
            log.info("[Seeder] Tenant '{}' ya existe — omitiendo.", slug);
            return;
        }

        CreateTenantRequest req = new CreateTenantRequest();
        req.setName("Fisiosalud SAS");
        req.setSlug(slug);
        req.setOwnerName("Administrador Fisiosalud");
        req.setOwnerEmail("admin@fisiosalud.ecosaludmarket.com");
        req.setPhone("+57 300 000 0002");
        req.setCity("Colombia");
        req.setCountry("CO");
        req.setSpecialty("Fisioterapia · Fonoaudiología · Psicología · Terapia Ocupacional");
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
