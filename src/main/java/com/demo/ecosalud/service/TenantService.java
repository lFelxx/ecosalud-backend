package com.demo.ecosalud.service;

import java.util.List;

import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.dto.TenantDTO;

/**
 * Contrato del servicio de gestión de tenants.
 * Solo accesible para el super-administrador de la plataforma.
 */
public interface TenantService {

    /** Registra una nueva clínica y crea su schema en PostgreSQL. */
    TenantDTO createTenant(CreateTenantRequest request);

    /** Retorna todos los tenants registrados. */
    List<TenantDTO> getAllTenants();

    /** Busca un tenant por su slug único. */
    TenantDTO getBySlug(String slug);

    /** Activa o desactiva una clínica. */
    TenantDTO toggleActive(Long tenantId, boolean active);

    /** Cambia el plan de suscripción de una clínica. */
    TenantDTO changePlan(Long tenantId, String plan);

    /** Activa o desactiva la exención de pago (para fundadores y promos). */
    TenantDTO toggleBillingExemption(Long tenantId, boolean exempt);

    /** Verifica el dominio propio de una clínica y lo activa. */
    TenantDTO verifyCustomDomain(Long tenantId, String domain);
}
