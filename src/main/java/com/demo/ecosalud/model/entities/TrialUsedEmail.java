package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Registro de emails que ya consumieron su período de prueba gratuito.
 *
 * <p>Almacenado en {@code public.trial_used_emails}.
 * Se inserta cuando se crea un tenant nuevo via onboarding.
 * Se consulta al inicio del onboarding para prevenir re-uso del trial.</p>
 *
 * <p>Solo almacena el hash del email (SHA-256) para privacidad.</p>
 */
@Entity
@Table(name = "trial_used_emails", schema = "public",
       indexes = @Index(name = "idx_trial_email", columnList = "email", unique = true))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialUsedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email en minúsculas (no sensible — solo para lookup). */
    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    /** Tenant que se creó con este email (slug). */
    @Column(name = "tenant_slug", length = 100)
    private String tenantSlug;

    @CreationTimestamp
    @Column(name = "used_at", nullable = false, updatable = false)
    private LocalDateTime usedAt;
}
