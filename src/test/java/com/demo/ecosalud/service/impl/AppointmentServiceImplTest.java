package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.enums.AppointmentSatus;
import com.demo.ecosalud.exception.BusinessRuleException;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.AppointmentDTO;
import com.demo.ecosalud.model.entities.Appointment;
import com.demo.ecosalud.model.entities.Catalog;
import com.demo.ecosalud.model.entities.Therapist;
import com.demo.ecosalud.model.entities.User;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.CatalogRepository;
import com.demo.ecosalud.repository.TherapistRepository;
import com.demo.ecosalud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private TherapistRepository therapistRepository;
    @Mock private CatalogRepository catalogRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User user;
    private Therapist therapist;
    private Catalog catalog;
    private Appointment appointment;
    private AppointmentDTO appointmentDTO;
    private final LocalDateTime futureDate = LocalDateTime.now().plusDays(5);

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Paciente Test");
        user.setEmail("paciente@test.com");

        User therapistUser = new User();
        therapistUser.setId(2L);
        therapistUser.setName("Terapeuta Test");

        therapist = new Therapist();
        therapist.setId(1L);
        therapist.setUser(therapistUser);
        therapist.setAvailable(true);

        catalog = new Catalog();
        catalog.setId(1L);
        catalog.setName("Masaje");
        catalog.setAvailability(true);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setUser(user);
        appointment.setTherapist(therapist);
        appointment.setCatalog(catalog);
        appointment.setDate(futureDate);
        appointment.setStatus(AppointmentSatus.CONFIRMADA);

        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setUserId(1L);
        appointmentDTO.setTherapistId(1L);
        appointmentDTO.setCatalogId(1L);
        appointmentDTO.setDate(futureDate);
    }

    @Test
    void scheduleAppointment_exitoso_retornaDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(appointmentRepository.existsByUserIdAndDateAndStatusNot(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsByTherapistIdAndDateAndStatusNot(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(appointment);

        AppointmentDTO result = appointmentService.scheduleAppointment(appointmentDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(appointmentRepository).save(any());
    }

    @Test
    void scheduleAppointment_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paciente no encontrado");
    }

    @Test
    void scheduleAppointment_terapeutaNoExiste_lanzaExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Terapeuta no encontrado");
    }

    @Test
    void scheduleAppointment_terapeutaNoDisponible_lanzaExcepcion() {
        therapist.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("terapeuta seleccionado no está disponible");
    }

    @Test
    void scheduleAppointment_catalogNoExiste_lanzaExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(catalogRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Servicio no encontrado");
    }

    @Test
    void scheduleAppointment_catalogNoDisponible_lanzaExcepcion() {
        catalog.setAvailability(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("servicio seleccionado no está disponible");
    }

    @Test
    void scheduleAppointment_conflictoPaciente_lanzaExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(appointmentRepository.existsByUserIdAndDateAndStatusNot(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene una cita agendada");
    }

    @Test
    void scheduleAppointment_conflictoTerapeuta_lanzaExcepcion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(therapistRepository.findById(1L)).thenReturn(Optional.of(therapist));
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(appointmentRepository.existsByUserIdAndDateAndStatusNot(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsByTherapistIdAndDateAndStatusNot(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(appointmentDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("terapeuta no tiene disponibilidad");
    }

    @Test
    void getAppointmentById_existe_retornaDTO() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentDTO result = appointmentService.getAppointmentById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getAppointmentById_noExiste_lanzaExcepcion() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllAppointments_retornaLista() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));

        List<AppointmentDTO> results = appointmentService.getAllAppointments();

        assertThat(results).hasSize(1);
    }

    @Test
    void getAppointmentsByUser_usuarioExiste_retornaLista() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(appointmentRepository.findByUserId(1L)).thenReturn(List.of(appointment));

        List<AppointmentDTO> results = appointmentService.getAppointmentsByUser(1L);

        assertThat(results).hasSize(1);
    }

    @Test
    void getAppointmentsByUser_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.getAppointmentsByUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rescheduleAppointment_exitoso_retornaReprogramada() {
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(10);
        Appointment reprogramada = buildAppointment(AppointmentSatus.REPROGRAMADA, nuevaFecha);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByUserIdAndDateAndStatusNot(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsByTherapistIdAndDateAndStatusNot(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(reprogramada);

        AppointmentDTO result = appointmentService.rescheduleAppointment(1L, nuevaFecha);

        assertThat(result.getStatus()).isEqualTo(AppointmentSatus.REPROGRAMADA);
    }

    @Test
    void rescheduleAppointment_citaCancelada_lanzaExcepcion() {
        appointment.setStatus(AppointmentSatus.CANCELADA);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, futureDate))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede reprogramar una cita cancelada");
    }

    @Test
    void rescheduleAppointment_fechaPasada_lanzaExcepcion() {
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(1);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, fechaPasada))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La nueva fecha debe ser en el futuro");
    }

    @Test
    void cancelAppointment_exitoso_retornaCancelada() {
        Appointment cancelada = buildAppointment(AppointmentSatus.CANCELADA, futureDate);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(cancelada);

        AppointmentDTO result = appointmentService.cancelAppointment(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentSatus.CANCELADA);
    }

    @Test
    void cancelAppointment_yaCancelada_lanzaExcepcion() {
        appointment.setStatus(AppointmentSatus.CANCELADA);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya se encuentra cancelada");
    }

    @Test
    void confirmAppointment_exitoso_retornaConfirmada() {
        appointment.setStatus(AppointmentSatus.PENDIENTE);
        Appointment confirmada = buildAppointment(AppointmentSatus.CONFIRMADA, futureDate);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any())).thenReturn(confirmada);

        AppointmentDTO result = appointmentService.confirmAppointment(1L);

        assertThat(result.getStatus()).isEqualTo(AppointmentSatus.CONFIRMADA);
    }

    @Test
    void confirmAppointment_cancelada_lanzaExcepcion() {
        appointment.setStatus(AppointmentSatus.CANCELADA);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.confirmAppointment(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No se puede confirmar una cita cancelada");
    }

    @Test
    void deleteAppointment_existe_elimina() {
        when(appointmentRepository.existsById(1L)).thenReturn(true);

        appointmentService.deleteAppointment(1L);

        verify(appointmentRepository).deleteById(1L);
    }

    @Test
    void deleteAppointment_noExiste_lanzaExcepcion() {
        when(appointmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAppointmentsByTherapist_terapeutaExiste_retornaLista() {
        when(therapistRepository.existsById(1L)).thenReturn(true);
        when(appointmentRepository.findByTherapistId(1L)).thenReturn(List.of(appointment));

        List<AppointmentDTO> results = appointmentService.getAppointmentsByTherapist(1L);

        assertThat(results).hasSize(1);
    }

    @Test
    void getAppointmentsByTherapist_terapeutaNoExiste_lanzaExcepcion() {
        when(therapistRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.getAppointmentsByTherapist(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAppointmentsByStatus_retornaListaFiltrada() {
        when(appointmentRepository.findByStatus(AppointmentSatus.CONFIRMADA)).thenReturn(List.of(appointment));

        List<AppointmentDTO> results = appointmentService.getAppointmentsByStatus(AppointmentSatus.CONFIRMADA);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(AppointmentSatus.CONFIRMADA);
    }

    @Test
    void rescheduleAppointment_conflictoPacienteEnNuevaFecha_lanzaExcepcion() {
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(10);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByUserIdAndDateAndStatusNot(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, nuevaFecha))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene una cita agendada");
    }

    @Test
    void rescheduleAppointment_conflictoTerapeutaEnNuevaFecha_lanzaExcepcion() {
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(10);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByUserIdAndDateAndStatusNot(any(), any(), any())).thenReturn(false);
        when(appointmentRepository.existsByTherapistIdAndDateAndStatusNot(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, nuevaFecha))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("terapeuta no tiene disponibilidad");
    }

    private Appointment buildAppointment(AppointmentSatus status, LocalDateTime date) {
        Appointment a = new Appointment();
        a.setId(1L);
        a.setUser(user);
        a.setTherapist(therapist);
        a.setCatalog(catalog);
        a.setDate(date);
        a.setStatus(status);
        return a;
    }
}
