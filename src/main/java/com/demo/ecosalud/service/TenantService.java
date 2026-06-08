package com.demo.ecosalud.service;

import java.util.List;

import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.dto.ResetAdminPasswordRequest;
import com.demo.ecosalud.model.dto.TenantDTO;
import com.demo.ecosalud.model.dto.TenantStatsDTO;
import com.demo.ecosalud.model.dto.UpdateTenantRequest;

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

    /**
     * Actualiza la información editable de una clínica.
     * Solo se modifican los campos no-nulos del request.
     */
    TenantDTO updateTenant(Long tenantId, UpdateTenantRequest request);

    /**
     * Restablece la contraseña del administrador principal de una clínica.
     * Busca al usuario en {@code public.users} por el {@code ownerEmail} del tenant.
     */
    void resetAdminPassword(Long tenantId, ResetAdminPasswordRequest request);

    /**
     * Devuelve estadísticas de uso del tenant: usuarios, citas, especialistas, etc.
     * Útil para el equipo de soporte.
     */
    TenantStatsDTO getTenantStats(Long tenantId);

    /**
     * Elimina completamente un tenant: su registro en {@code public.tenants}
     * y su schema PostgreSQL con todos sus datos.
     *
     * <p><strong>IRREVERSIBLE.</strong> Solo ejecutar tras confirmación explícita.</p>
     */
    void deleteTenant(Long tenantId);
}
