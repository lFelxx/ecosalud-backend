package com.demo.ecosalud.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO para crear, actualizar y retornar datos de un servicio del catálogo.
 */
@Data
public class CatalogDTO {

    private Long id;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    private String name;

    @NotBlank(message = "La descripción del servicio es obligatoria")
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    private Double price;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
    private Integer duration;

    private Boolean availability;

    @NotBlank(message = "La categoría del servicio es obligatoria")
    private String category;

    /** Lista de beneficios del servicio; cada elemento es el texto de un beneficio. */
    private List<String> benefits;
}
