package com.demo.ecosalud.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Configuración global de la plataforma Ecosalud Market (clave-valor).
 *
 * <p>Vive en {@code public.platform_settings}.
 * Editable solo por el super-admin. Leída públicamente via
 * {@code GET /api/public/settings}.</p>
 *
 * <p><strong>Claves predefinidas:</strong>
 * <ul>
 *   <li>{@code social.whatsapp}   — número WhatsApp con prefijo país ej. +573001234567</li>
 *   <li>{@code social.email}      — email de contacto de la plataforma</li>
 *   <li>{@code social.instagram}  — URL perfil Instagram</li>
 *   <li>{@code social.facebook}   — URL perfil Facebook</li>
 *   <li>{@code social.twitter}    — URL perfil Twitter/X</li>
 *   <li>{@code legal.privacy}     — texto completo de la política de privacidad</li>
 *   <li>{@code legal.terms}       — texto completo de los términos de servicio</li>
 *   <li>{@code contact.email}     — email de soporte</li>
 *   <li>{@code contact.phone}     — teléfono de soporte</li>
 *   <li>{@code contact.address}   — dirección física</li>
 *   <li>{@code contact.schedule}  — horario de atención</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "platform_settings", schema = "public",
       indexes = @Index(name = "idx_platform_settings_key", columnList = "setting_key", unique = true))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    /** Descripción visible en el panel super-admin. */
    @Column(name = "description", length = 300)
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
