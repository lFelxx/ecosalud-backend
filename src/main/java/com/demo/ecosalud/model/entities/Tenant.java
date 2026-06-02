package com.demo.ecosalud.model.entities;

import java.time.LocalDateTime;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una clínica o consultorio registrado en la plataforma.
 *
 * <p>Cada tenant tiene su propio schema en PostgreSQL ({@code tenant_{slug}.*})
 * y opcionalmente su propio dominio web.</p>
 *
 * <p>Esta entidad vive en el schema {@code public} y es gestionada
 * exclusivamente por el super-administrador de la plataforma.</p>
 *
 * <p>Tenants fundadores actuales:</p>
 * <ul>
 *   <li>ecosalud_camacho — Dra. Angélica Camacho (Medicina Integrativa)</li>
 *   <li>fisiosalud       — Fisiosalud SAS (IPS, fisioterapia y más)</li>
 * </ul>
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    schema = "public",
    name   = "tenants",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tenant_slug",          columnNames = "slug"),
        @UniqueConstraint(name = "uk_tenant_subdomain",     columnNames = "subdomain"),
        @UniqueConstraint(name = "uk_tenant_custom_domain", columnNames = "custom_domain"),
        @UniqueConstraint(name = "uk_tenant_schema",        columnNames = "schema_name")
    }
)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identidad ───────────────────────────────────────────────────────────

    /** Nombre legal o comercial de la clínica. */
    @Column(nullable = false)
    private String name;

    /**
     * Identificador único en URL (solo letras minúsculas, números y guiones).
     * Ejemplo: {@code ecosalud-camacho}, {@code fisiosalud}.
     */
    @Column(nullable = false, length = 100)
    private String slug;

    /**
     * Nombre del schema PostgreSQL asignado a este tenant.
     * Ejemplo: {@code tenant_ecosalud_camacho}, {@code tenant_fisiosalud}.
     */
    @Column(name = "schema_name", nullable = false, length = 100)
    private String schemaName;

    // ── Contacto y localización ──────────────────────────────────────────────

    @Column(name = "owner_email", nullable = false)
    private String ownerEmail;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    private String phone;
    private String address;
    private String city;

    @Builder.Default
    private String country = "CO";

    // ── Dominio y acceso ─────────────────────────────────────────────────────

    /**
     * Subdominio asignado automáticamente al registrar.
     * Formato: {@code [slug].ecosaludmarket.com}
     */
    @Column(nullable = false, length = 255)
    private String subdomain;

    /**
     * Dominio propio del cliente (Plan Pro o Clínica).
     * Ejemplo: {@code www.clinicaangelica.com}
     * Se activa después de verificar el CNAME.
     */
    @Column(name = "custom_domain", length = 255)
    private String customDomain;

    /**
     * {@code true} si el CNAME del dominio propio fue verificado exitosamente.
     */
    @Builder.Default
    @Column(name = "domain_verified")
    private Boolean domainVerified = false;

    // ── Personalización visual ───────────────────────────────────────────────

    /** Color primario en formato hex. Ejemplo: {@code #3DAA96} */
    @Builder.Default
    @Column(name = "primary_color", length = 7)
    private String primaryColor = "#3DAA96";

    /** URL o base64 del logo de la clínica. */
    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    /** Especialidad principal (ej. "Medicina Integrativa", "Fisioterapia"). */
    private String specialty;

    // ── Suscripción ──────────────────────────────────────────────────────────

    @Builder.Default
    @Column(name = "plan", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan = SubscriptionPlan.STARTER;

    @Builder.Default
    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType = AccountType.REGULAR;

    @Builder.Default
    @Column(name = "billing_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingStatus billingStatus = BillingStatus.ACTIVE;

    /** Código promocional activo (si aplica). */
    @Column(name = "promo_code", length = 50)
    private String promoCode;

    // ── Estado ───────────────────────────────────────────────────────────────

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "schema_initialized")
    private Boolean schemaInitialized = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Hooks JPA ────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
