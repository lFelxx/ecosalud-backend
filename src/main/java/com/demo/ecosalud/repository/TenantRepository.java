package com.demo.ecosalud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.demo.ecosalud.model.entities.Tenant;

/**
 * Repositorio JPA para la entidad {@link Tenant}.
 *
 * <p>Opera siempre sobre el schema {@code public} ya que los tenants
 * son datos de plataforma, no de un tenant específico.</p>
 */
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlug(String slug);

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
}
