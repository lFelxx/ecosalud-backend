package com.demo.ecosalud.model.entities;

import java.time.LocalDateTime;

import com.demo.ecosalud.enums.AppointmentSatus;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Representa una cita agendada en el sistema.
 * Vincula un paciente, un terapeuta y un servicio del catálogo en una fecha específica.
 */
@Entity
@Data
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /** Paciente que agenda la cita. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Terapeuta asignado para atender la cita. */
    @ManyToOne
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;

    /** Servicio del catálogo que se prestará en la cita. */
    @ManyToOne
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    /** Fecha y hora de inicio de la cita. */
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    /** Estado actual de la cita en su ciclo de vida. */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentSatus status;
}
