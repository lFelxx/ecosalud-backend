package com.demo.ecosalud.service;

import java.util.List;

import com.demo.ecosalud.model.dto.TherapistDTO;
import com.demo.ecosalud.model.dto.TherapistRegisterDTO;

/**
 * Contrato de negocio para la gestión de perfiles de terapeutas.
 */
public interface TherapistService {

    /**
     * Registro unificado: crea la cuenta de usuario con rol THERAPIST y su perfil
     * profesional en una sola transacción. Los datos del perfil son opcionales.
     *
     * @param dto datos de cuenta + datos opcionales del perfil
     * @return perfil del terapeuta recién creado
     */
    TherapistDTO registerTherapist(TherapistRegisterDTO dto);

    /**
     * Obtiene el perfil de un terapeuta por su ID de perfil.
     *
     * @param id identificador del perfil Therapist
     * @return datos del terapeuta
     */
    TherapistDTO getTherapistById(Long id);

    /**
     * Obtiene el perfil de un terapeuta a partir del ID de su usuario.
     *
     * @param userId identificador del User asociado
     * @return datos del terapeuta
     */
    TherapistDTO getTherapistByUserId(Long userId);

    /**
     * Retorna todos los terapeutas registrados en el sistema.
     *
     * @return lista de todos los terapeutas
     */
    List<TherapistDTO> getAllTherapists();

    /**
     * Retorna solo los terapeutas disponibles para agendar citas.
     *
     * @return lista de terapeutas con {@code available = true}
     */
    List<TherapistDTO> getAvailableTherapists();

    /**
     * Retorna los terapeutas de una especialidad específica.
     *
     * @param specialty nombre de la especialidad a filtrar
     * @return lista de terapeutas con esa especialidad
     */
    List<TherapistDTO> getTherapistsBySpecialty(String specialty);

    /**
     * Actualiza los datos del perfil profesional de un terapeuta.
     *
     * @param id           ID del perfil a actualizar
     * @param therapistDTO nuevos datos del perfil
     * @return perfil actualizado
     */
    TherapistDTO updateTherapist(Long id, TherapistDTO therapistDTO);

    /**
     * Invierte la disponibilidad del terapeuta (disponible ↔ no disponible).
     *
     * @param id ID del perfil
     * @return perfil actualizado con la nueva disponibilidad
     */
    TherapistDTO toggleAvailability(Long id);

    /**
     * Elimina permanentemente el perfil de un terapeuta.
     *
     * @param id ID del perfil a eliminar
     */
    void deleteTherapist(Long id);
}
