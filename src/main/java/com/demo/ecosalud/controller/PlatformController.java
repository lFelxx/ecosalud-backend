package com.demo.ecosalud.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.demo.ecosalud.model.dto.CreateTenantRequest;
import com.demo.ecosalud.model.dto.ResetAdminPasswordRequest;
import com.demo.ecosalud.model.dto.RevenueDTO;
import com.demo.ecosalud.model.dto.TenantDTO;
import com.demo.ecosalud.model.dto.TenantStatsDTO;
import com.demo.ecosalud.model.dto.UpdateTenantRequest;
import com.demo.ecosalud.service.RevenueService;
import com.demo.ecosalud.service.TenantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador del panel de super-administración de la plataforma.
 *
 * <p>Ruta base: {@code /api/platform}</p>
 * <p>Solo accesible con rol {@code PLATFORM_ADMIN}.</p>
 *
 * <p>Operaciones:</p>
 * <ul>
 *   <li>POST   /tenants           — Registrar nueva clínica</li>
 *   <li>GET    /tenants           — Listar todas las clínicas</li>
 *   <li>GET    /tenants/{slug}    — Detalle de una clínica</li>
 *   <li>PATCH  /tenants/{id}/active         — Activar/desactivar</li>
 *   <li>PATCH  /tenants/{id}/plan           — Cambiar plan</li>
 *   <li>PATCH  /tenants/{id}/billing-exempt — Toggle exención de pago</li>
 *   <li>PATCH  /tenants/{id}/custom-domain  — Verificar dominio propio</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformController {

    private final TenantService  tenantService;
    private final RevenueService revenueService;

    // ── Tenants ──────────────────────────────────────────────────────────────

    /**
     * Registra una nueva clínica y crea su schema en PostgreSQL.
     * Retorna 201 Created con el DTO del tenant.
     */
    @PostMapping("/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDTO crearTenant(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }

    /** Retorna la lista completa de clínicas registradas en la plataforma. */
    @GetMapping("/tenants")
    public List<TenantDTO> listarTenants() {
        return tenantService.getAllTenants();
    }

    /** Retorna el detalle de una clínica por su slug. */
    @GetMapping("/tenants/{slug}")
    public TenantDTO obtenerTenant(@PathVariable String slug) {
        return tenantService.getBySlug(slug);
    }

    /** Activa o desactiva una clínica. Parámetro: {@code ?active=true|false} */
    @PatchMapping("/tenants/{id}/active")
    public TenantDTO toggleActive(@PathVariable Long id,
                                  @RequestParam boolean active) {
        return tenantService.toggleActive(id, active);
    }

    /** Cambia el plan de suscripción de una clínica. Body: {@code { "plan": "PRO" }} */
    @PatchMapping("/tenants/{id}/plan")
    public TenantDTO cambiarPlan(@PathVariable Long id,
                                 @RequestParam String plan) {
        return tenantService.changePlan(id, plan);
    }

    /**
     * Activa o desactiva la exención de pago de una clínica.
     * Usar para fundadores y promociones especiales.
     * Parámetro: {@code ?exempt=true|false}
     */
    @PatchMapping("/tenants/{id}/billing-exempt")
    public TenantDTO toggleBillingExemption(@PathVariable Long id,
                                             @RequestParam boolean exempt) {
        return tenantService.toggleBillingExemption(id, exempt);
    }

    /**
     * Marca el dominio propio de una clínica como verificado.
     * Parámetro: {@code ?domain=www.clinicaangelica.com}
     * Solo activar tras confirmar que el CNAME fue configurado correctamente.
     */
    @PatchMapping("/tenants/{id}/custom-domain")
    public TenantDTO verificarDominio(@PathVariable Long id,
                                      @RequestParam String domain) {
        return tenantService.verifyCustomDomain(id, domain);
    }

    /**
     * Actualiza la información editable de una clínica.
     * Solo se modifican los campos presentes en el body (patch semántico).
     */
    @PutMapping("/tenants/{id}")
    public TenantDTO actualizarTenant(@PathVariable Long id,
                                      @RequestBody UpdateTenantRequest request) {
        return tenantService.updateTenant(id, request);
    }

    /**
     * Restablece la contraseña del administrador de una clínica.
     * Acción de soporte — requiere verificar la identidad del cliente previamente.
     */
    @PatchMapping("/tenants/{id}/admin-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetAdminPassword(@PathVariable Long id,
                                   @Valid @RequestBody ResetAdminPasswordRequest request) {
        tenantService.resetAdminPassword(id, request);
    }

    /**
     * Retorna estadísticas de uso del tenant: usuarios, citas, especialistas, etc.
     * Útil para el equipo de soporte al revisar el estado de una clínica.
     */
    @GetMapping("/tenants/{id}/stats")
    public TenantStatsDTO obtenerStats(@PathVariable Long id) {
        return tenantService.getTenantStats(id);
    }

    /**
     * Elimina permanentemente una clínica: schema PostgreSQL + datos + registro.
     * <strong>IRREVERSIBLE.</strong> Requiere confirmación con header {@code X-Confirm-Delete: true}.
     */
    @DeleteMapping("/tenants/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarTenant(@PathVariable Long id,
                               @RequestHeader(value = "X-Confirm-Delete", required = false) String confirm) {
        if (!"true".equalsIgnoreCase(confirm)) {
            throw new IllegalArgumentException(
                    "Eliminación no confirmada. Envía el header 'X-Confirm-Delete: true'.");
        }
        tenantService.deleteTenant(id);
    }

    // ── Revenue ──────────────────────────────────────────────────────────────

    /**
     * Dashboard de ingresos de la plataforma.
     *
     * <p>Retorna MRR, ARR, distribución por plan, tasa de conversión
     * y crecimiento mensual de los últimos 6 meses.</p>
     *
     * <p>Solo accesible por el super-administrador.</p>
     */
    @GetMapping("/revenue")
    public RevenueDTO getRevenue() {
        return revenueService.getRevenueSummary();
    }
}
