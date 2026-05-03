package com.demo.ecosalud.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.model.entities.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Boolean existsByUserId(Long userId);
    Boolean existsByServiceId(Long serviceId);
    Boolean existsByDate(LocalDateTime date);
    Boolean existsByStatus(AppointmentSatus status);
    
}
