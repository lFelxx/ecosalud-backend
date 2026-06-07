package com.demo.ecosalud.model.dto.fhir;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Tipos de componente reutilizables de <strong>HL7 FHIR R4</strong>.
 *
 * <p>Referencia oficial: <a href="https://hl7.org/fhir/R4/datatypes.html">
 * https://hl7.org/fhir/R4/datatypes.html</a></p>
 *
 * <p>Todos los tipos se anotan con {@code @JsonInclude(NON_NULL)}
 * para cumplir con la spec FHIR, que prohíbe propiedades {@code null}
 * en la representación JSON.</p>
 */
public final class FhirTypes {

    private FhirTypes() { /* Utility class — no instances */ }

    // ── Coding ───────────────────────────────────────────────────────────────

    /**
     * Referencia a un código definido en un sistema de terminología.
     * <a href="https://hl7.org/fhir/R4/datatypes.html#Coding">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Coding {
        /** URI que identifica el sistema de código (p.ej. SNOMED CT, LOINC). */
        private String system;
        /** Versión del sistema de código (opcional). */
        private String version;
        /** Símbolo del código en el sistema. */
        private String code;
        /** Representación legible del código. */
        private String display;
    }

    // ── CodeableConcept ───────────────────────────────────────────────────────

    /**
     * Valor representado por un código de un sistema de terminología.
     * <a href="https://hl7.org/fhir/R4/datatypes.html#CodeableConcept">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CodeableConcept {
        /** Lista de codificaciones que representan el concepto. */
        private List<Coding> coding;
        /** Representación textual del concepto (útil cuando no hay código exacto). */
        private String text;
    }

    // ── Identifier ────────────────────────────────────────────────────────────

    /**
     * Identificador externo para un recurso.
     * <a href="https://hl7.org/fhir/R4/datatypes.html#Identifier">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Identifier {
        /** Propósito del identificador: {@code usual | official | temp | secondary | old}. */
        private String use;
        /** Tipo del identificador (p.ej. CC, CE, PA para Colombia). */
        private CodeableConcept type;
        /** URI que define el espacio de nombres del identificador. */
        private String system;
        /** Valor del identificador dentro del sistema. */
        private String value;
    }

    // ── HumanName ─────────────────────────────────────────────────────────────

    /**
     * Nombre de una persona en formato FHIR.
     * <a href="https://hl7.org/fhir/R4/datatypes.html#HumanName">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HumanName {
        /** Uso del nombre: {@code official | nickname | maiden | temp | anonymous | old}. */
        private String use;
        /** Nombre completo como texto (alternativa a las partes individuales). */
        private String text;
        /** Apellido(s). */
        private String family;
        /** Nombre(s) de pila. */
        private List<String> given;
    }

    // ── ContactPoint ──────────────────────────────────────────────────────────

    /**
     * Datos de contacto de una persona u organización.
     * <a href="https://hl7.org/fhir/R4/datatypes.html#ContactPoint">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactPoint {
        /** Tipo de medio: {@code phone | fax | email | pager | url | sms | other}. */
        private String system;
        /** Valor del punto de contacto. */
        private String value;
        /** Uso: {@code home | work | temp | old | mobile}. */
        private String use;
        /** Orden de preferencia para este punto de contacto. */
        private Integer rank;
    }

    // ── Reference ─────────────────────────────────────────────────────────────

    /**
     * Referencia a otro recurso FHIR.
     * <a href="https://hl7.org/fhir/R4/references.html">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Reference {
        /** Referencia relativa o absoluta al recurso, p.ej. {@code "Patient/123"}. */
        private String reference;
        /** Texto legible que describe el recurso referenciado. */
        private String display;
    }

    // ── Period ────────────────────────────────────────────────────────────────

    /**
     * Período de tiempo definido por inicio y fin.
     * <a href="https://hl7.org/fhir/R4/datatypes.html#Period">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Period {
        /** Fecha/hora de inicio (FHIR dateTime: {@code YYYY-MM-DDThh:mm:ss+zz:zz}). */
        private String start;
        /** Fecha/hora de fin. Null si el período está abierto. */
        private String end;
    }

    // ── Meta ──────────────────────────────────────────────────────────────────

    /**
     * Metadatos de un recurso FHIR.
     * <a href="https://hl7.org/fhir/R4/resource.html#Meta">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        /** Versión lógica del recurso. Incrementa con cada actualización. */
        private String versionId;
        /** Fecha/hora de la última modificación del recurso. */
        private String lastUpdated;
        /** Lista de URLs de perfiles FHIR que este recurso declara cumplir. */
        private List<String> profile;
    }

    // ── BundleEntry ───────────────────────────────────────────────────────────

    /**
     * Entrada dentro de un {@code Bundle} FHIR.
     * <a href="https://hl7.org/fhir/R4/bundle.html#entry">Spec</a>
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BundleEntry {
        /** URL completa del recurso en este servidor FHIR. */
        private String fullUrl;
        /** El recurso FHIR en sí (Patient, Encounter, etc.). */
        private Object resource;
    }
}
