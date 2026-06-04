package com.demo.ecosalud.model.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Representa un tipo de servicio ofrecido por la clínica.
 * El catálogo es independiente de los terapeutas; la asociación
 * entre servicio y especialista ocurre al momento de agendar la cita.
 */
@Entity
@Data
@Table(name = "catalogs")
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Nombre del servicio (ej: Masaje terapéutico, Consulta psicológica). */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /** Descripción detallada del servicio para mostrar al paciente. */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Precio del servicio en la moneda local. */
    @Column(name = "price", nullable = false)
    private Double price;

    /** Duración estimada de la sesión en minutos. */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    /** Indica si el servicio está activo y puede ser agendado. */
    @Column(name = "availability", nullable = false)
    private Boolean availability = true;

    /** Categoría del servicio (ej: Desintoxicación, Energía, Inmunidad). */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * Lista de beneficios del servicio. Cada elemento es el texto descriptivo
     * de un beneficio (ej: "Reduce el estrés", "Mejora la movilidad").
     * Se persiste en la tabla secundaria {@code catalog_benefits}.
     */
    @ElementCollection
    @CollectionTable(name = "catalog_benefits", joinColumns = @JoinColumn(name = "catalog_id"))
    @Column(name = "benefit")
    private List<String> benefits;
}
