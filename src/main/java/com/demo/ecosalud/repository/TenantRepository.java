package com.demo.ecosalud.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.model.entities.Tenant;

/**
 * Repositorio JPA para la entidad {@link Tenant}.
 *
 * <p>Opera siempre sobre el schema {@code public} ya que los tenants
 * son datos de plataforma, no de un tenant específico.</p>
 */
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlug(String slug);

    Optional<Tenant> findBySchemaName(String schemaName);

    Optional<Tenant> findBySubdomain(String subdomain);

    Optional<Tenant> findByCustomDomain(String customDomain);

    Optional<Tenant> findByOwnerEmail(String ownerEmail);

    boolean existsBySlug(String slug);

    boolean existsByOwnerEmail(String ownerEmail);

    boolean existsByCustomDomain(String customDomain);

    /**
     * Busca un tenant por subdominio O por dominio propio verificado.
     * Usado por el {@code TenantFilter} para resolver el tenant de una petición.
     */
    @Query("""
        SELECT t FROM Tenant t
        WHERE t.subdomain = :domain
           OR (t.customDomain = :domain AND t.domainVerified = true)
        """)
    Optional<Tenant> findByAnyDomain(String domain);

    // ── Queries para el scheduler de trials ──────────────────────────────────

    /**
     * Tenants en período de prueba cuyo trial ya expiró.
     * Usado por {@link com.demo.ecosalud.scheduler.TrialExpirationJob} para cambiar
     * el estado a {@code OVERDUE}.
     *
     * @param status  debe ser {@code BillingStatus.TRIAL}
     * @param cutoff  fecha/hora límite ({@code now() - trialDurationDays})
     */
    List<Tenant> findByBillingStatusAndCreatedAtBeforeAndActiveTrue(
            BillingStatus status, LocalDateTime cutoff);

    /**
     * Tenants en período de prueba que expirarán pronto.
     * Usado para enviar el email de aviso 3 días antes del vencimiento.
     *
     * @param status debe ser {@code BillingStatus.TRIAL}
     * @param from   inicio del rango ({@code cutoff - 3 días})
     * @param to     fin del rango   ({@code cutoff})
     */
    List<Tenant> findByBillingStatusAndCreatedAtBetweenAndActiveTrue(
            BillingStatus status, LocalDateTime from, LocalDateTime to);
}
