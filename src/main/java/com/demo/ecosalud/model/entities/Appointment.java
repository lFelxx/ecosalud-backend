package com.demo.ecosalud.model.entities;

import java.time.LocalDateTime;

import com.demo.ecosalud.enums.AppointmentSatus;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentSatus status;
}
