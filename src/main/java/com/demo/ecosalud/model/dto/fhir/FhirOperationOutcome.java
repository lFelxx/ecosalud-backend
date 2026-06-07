package com.demo.ecosalud.model.dto.fhir;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Recurso <strong>FHIR R4 OperationOutcome</strong>.
 *
 * <p>Usado para comunicar errores y advertencias en formato FHIR.
 * Reemplaza las respuestas de error JSON genéricas en el contexto FHIR.</p>
 *
 * <p>Referencia: <a href="https://hl7.org/fhir/R4/operationoutcome.html">
 * https://hl7.org/fhir/R4/operationoutcome.html</a></p>
 *
 * <p>Ejemplo de 404:</p>
 * <pre>{@code
 * {
 *   "resourceType": "OperationOutcome",
 *   "issue": [{
 *     "severity": "error",
 *     "code": "not-found",
 *     "diagnostics": "Patient/999 no encontrado en este tenant."
 *   }]
 * }
 * }</pre>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirOperationOutcome {

    /**
     * Tipo del recurso — siempre {@code "OperationOutcome"}.
     */
    @JsonProperty("resourceType")
    @Builder.Default
    private String resourceType = "OperationOutcome";

    /** Lista de problemas reportados por la operación. */
    private List<Issue> issue;

    // ── Sub-tipos ─────────────────────────────────────────────────────────────

    /**
     * Detalle de un problema individual en el OperationOutcome.
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Issue {

        /**
         * Severidad del problema.
         * Valores: {@code fatal | error | warning | information}.
         */
        private String severity;

        /**
         * Código de error FHIR.
         * Valores comunes:
         * <ul>
         *   <li>{@code not-found} — recurso no existe</li>
         *   <li>{@code forbidden} — sin permisos</li>
         *   <li>{@code invalid} — datos inválidos</li>
         *   <li>{@code processing} — error de procesamiento</li>
         * </ul>
         */
        private String code;

        /**
         * Mensaje técnico legible que describe el problema.
         * Puede incluir el tipo y ID del recurso afectado.
         */
        private String diagnostics;
    }
}
