package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.SpecialistDTO;

/** Contrato del servicio de perfil del especialista. */
public interface SpecialistService {

    /**
     * Devuelve el perfil del especialista principal del tenant.
     * Si no existe, retorna un DTO vacío (nunca lanza excepción).
     */
    SpecialistDTO get();

    /**
     * Upsert del perfil del especialista.
     * Crea el registro si no existe; actualiza si ya existe.
     */
    SpecialistDTO upsert(SpecialistDTO dto);
}
