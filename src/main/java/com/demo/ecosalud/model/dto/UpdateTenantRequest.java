package com.demo.ecosalud.model.dto;

import lombok.Data;

/**
 * DTO para actualizar la información de una clínica desde el panel super-admin.
 *
 * <p>Todos los campos son opcionales: solo se actualiza lo que venga no-nulo.</p>
 */
@Data
public class UpdateTenantRequest {

    // ── Identidad ─────────────────────────────────────────────────────────────
    private String name;
    private String specialty;

    // ── Contacto ──────────────────────────────────────────────────────────────
    private String ownerName;
    private String ownerEmail;
    private String phone;
    private String address;
    private String city;
    private String country;

    // ── Visual ────────────────────────────────────────────────────────────────
    private String primaryColor;
    private String logoUrl;

    // ── Dominio ───────────────────────────────────────────────────────────────
    private String customDomain;

    // ── Soporte / notas internas ──────────────────────────────────────────────
    private String promoCode;
}
