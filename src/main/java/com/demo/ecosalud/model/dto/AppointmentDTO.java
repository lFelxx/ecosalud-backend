package com.demo.ecosalud.model.dto;

import java.time.LocalDateTime;

import com.demo.ecosalud.enums.AppointmentSatus;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para crear, actualizar y retornar datos de una cita.
 * El frontend puede elegir primero especialista o primero fecha; el backend
 * solo necesita recibir los tres datos (catalogId, therapistId, date) al confirmar.
 */
@Data
public class AppointmentDTO {

    private Long id;

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long userId;

    @NotNull(message = "El ID del terapeuta es obligatorio")
    private Long therapistId;

    @NotNull(message = "El ID del servicio del catálogo es obligatorio")
    private Long catalogId;

    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha de la cita debe ser en el futuro")
    private LocalDateTime date;

    private AppointmentSatus status;

    // Campos de respuesta: nombres para evitar llamadas adicionales desde el frontend
    private String userName;
    private String therapistName;
    private String catalogName;
}
