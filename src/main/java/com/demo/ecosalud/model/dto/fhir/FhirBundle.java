package com.demo.ecosalud.model.dto.fhir;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Recurso <strong>FHIR R4 Bundle</strong>.
 *
 * <p>Contenedor para colecciones de recursos FHIR.
 * Usado como respuesta de las operaciones de búsqueda ({@code search-type}).</p>
 *
 * <p>Referencia: <a href="https://hl7.org/fhir/R4/bundle.html">
 * https://hl7.org/fhir/R4/bundle.html</a></p>
 *
 * <p>Tipos de Bundle usados en Ecosalud:</p>
 * <ul>
 *   <li>{@code searchset} — resultado de {@code GET /fhir/Patient},
 *       {@code GET /fhir/Encounter}</li>
 * </ul>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FhirBundle {

    /**
     * Tipo del recurso — siempre {@code "Bundle"}.
     * Obligatorio en la representación JSON según la spec FHIR.
     */
    @JsonProperty("resourceType")
    @Builder.Default
    private String resourceType = "Bundle";

    /** Identificador único de este Bundle. */
    private String id;

    /** Metadatos del Bundle. */
    private FhirTypes.Meta meta;

    /**
     * Tipo de Bundle.
     * Para búsquedas: {@code "searchset"}.
     */
    private String type;

    /**
     * Número total de recursos que coinciden con la búsqueda.
     * Puede ser mayor que la cantidad de entradas en {@code entry}
     * si se usa paginación (no implementada en v1).
     */
    private Integer total;

    /**
     * Lista de entradas del Bundle.
     * Cada entrada contiene la URL del recurso y el recurso en sí.
     */
    private List<FhirTypes.BundleEntry> entry;
}
