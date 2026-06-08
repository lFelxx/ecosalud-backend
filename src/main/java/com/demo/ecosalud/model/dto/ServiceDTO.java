package com.demo.ecosalud.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO para servicios / terapias de la clínica. */
@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String category;
    private String description;
    /** Beneficios separados por '\n' (el frontend los divide por línea). */
    private String benefits;
    private Integer durationMinutes;
    private BigDecimal priceCop;
    private String imageUrl;
    private boolean active;
    private LocalDateTime createdAt;
}
