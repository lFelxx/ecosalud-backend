package com.demo.ecosalud.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Información pública de la clínica activa (tenant actual).
 *
 * <p>Devuelta por {@code GET /api/public/info} sin requerir autenticación.
 * Usada por el frontend para construir el JSON-LD (schema.org) de SEO.</p>
 *
 * <p>No expone datos sensibles: solo nombre, contacto y ubicación visibles
 * en cualquier directorio público.</p>
 */
@Data
@Builder
public class PublicClinicInfoDTO {

    /** Nombre comercial de la clínica. */
    private String clinicName;

    /** Especialidad principal (ej. "Medicina Integrativa"). */
    private String specialty;

    /** Teléfono de contacto público. Puede ser null. */
    private String phone;

    /** Dirección física. Puede ser null. */
    private String address;

    /** Ciudad de operación. */
    private String city;

    /** Código de país ISO 3166-1 alfa-2 (ej. "CO"). */
    private String country;

    /** Subdominio asignado (ej. "dra-angelica-camacho.ecosaludmarket.com"). */
    private String subdomain;

    /** Color primario en hex (ej. "#3DAA96"). */
    private String primaryColor;
}
