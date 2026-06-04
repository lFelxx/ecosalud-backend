package com.demo.ecosalud.model.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RescheduleRequestDTO {

    @NotNull(message = "La nueva fecha es obligatoria")
    @Future(message = "La nueva fecha debe ser en el futuro")
    private LocalDateTime newDate;
}
