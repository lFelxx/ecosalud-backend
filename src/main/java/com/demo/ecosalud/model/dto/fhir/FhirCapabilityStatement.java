package com.demo.ecosalud.model.dto.fhir;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Recurso <strong>FHIR R4 CapabilityStatement</strong>.
 *
 * <p>Describe las capacidades de este servidor FHIR: qué recursos soporta,
 * qué operaciones permite y qué formato de datos acepta.</p>
 *
 * <p>Se expone en {@code GET /api/fhir/metadata} (público, sin JWT).
 * Es el punto de entrada estándar para descubrir las capacidades del servidor.</p>
 *
 * <p>Referencia: <a href="https://hl7.org/fhir/R4/capabilitystatement.html">
 * https://hl7.org/fhir/R4/capabilitystatement.html</a></p>
 *
 * <p>Relevancia para Colombia — <strong>Res. 2654/2019 MinSalud</strong>:</p>
 * <ul>
 *   <li>Interoperabilidad HCE mediante HL7 FHIR R4</li>
 *   <li>Integración con SISPRO (Sistema Integral de Información de la Protección Social)</li>
 *   <li>Identificación de pacientes con documentos colombianos (CC, CE, PA)</li>
 * </ul>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirCapabilityStatement {

    @JsonProperty("resourceType")
    @Builder.Default
    private String resourceType = "CapabilityStatement";

    /** Identificador único de esta declaración de capacidades. */
    private String id;

    /** Estado: {@code draft | active | retired | unknown}. */
    private String status;

    /** Fecha de publicación de esta declaración (FHIR dateTime). */
    private String date;

    /** Nombre de la organización publicadora. */
    private String publisher;

    /** Descripción legible de este servidor FHIR. */
    private String description;

    /** Tipo de recurso de capacidades: {@code instance} (servidor concreto). */
    private String kind;

    /** Información sobre el software que implementa este servidor FHIR. */
    private Software software;

    /** Versión FHIR implementada: siempre {@code "4.0.1"} (R4). */
    private String fhirVersion;

    /**
     * Formatos de datos aceptados.
     * Para Ecosalud: {@code ["json", "application/fhir+json"]}.
     */
    private List<String> format;

    /**
     * Lista de interfaces REST soportadas por el servidor.
     * Ecosalud implementa únicamente el modo {@code server}.
     */
    private List<Rest> rest;

    // ── Sub-tipos ─────────────────────────────────────────────────────────────

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Software {
        /** Nombre del producto de software. */
        private String name;
        /** Versión del software. */
        private String version;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Rest {
        /** Modo: {@code client | server}. */
        private String mode;
        /** Descripción de la interfaz REST. */
        private String documentation;
        /** Seguridad del endpoint REST. */
        private Security security;
        /** Lista de recursos soportados por este endpoint. */
        private List<Resource> resource;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Security {
        /** Si se soporta CORS. */
        private Boolean cors;
        /** Mecanismos de seguridad: JWT Bearer, SMART-on-FHIR, etc. */
        private List<FhirTypes.CodeableConcept> service;
        /** Descripción del mecanismo de seguridad. */
        private String description;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Resource {
        /** Tipo de recurso FHIR: {@code Patient}, {@code Encounter}, etc. */
        private String type;
        /** URL del perfil que este recurso declara cumplir. */
        private String profile;
        /** Interacciones soportadas para este recurso. */
        private List<Interaction> interaction;
        /** Parámetros de búsqueda soportados. */
        private List<SearchParam> searchParam;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Interaction {
        /**
         * Código de la interacción.
         * Valores: {@code read | vread | update | patch | delete |
         * history-instance | history-type | create | search-type}.
         */
        private String code;
        /** Descripción opcional de la interacción. */
        private String documentation;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchParam {
        /** Nombre del parámetro de búsqueda (p.ej. {@code patient}, {@code status}). */
        private String name;
        /**
         * Tipo del parámetro.
         * Valores: {@code number | date | string | token | reference | composite | quantity | uri | special}.
         */
        private String type;
        /** Descripción del parámetro. */
        private String documentation;
    }
}
