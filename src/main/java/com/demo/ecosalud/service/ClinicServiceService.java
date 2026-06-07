package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.ServiceDTO;

import java.util.List;

/** Contrato del servicio de terapias/servicios de la clínica. */
public interface ClinicServiceService {

    List<ServiceDTO> getAll();

    ServiceDTO getById(Long id);

    ServiceDTO create(ServiceDTO dto);

    ServiceDTO update(Long id, ServiceDTO dto);

    void delete(Long id);
}
