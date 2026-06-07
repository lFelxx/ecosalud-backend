package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.ConsentDTO;

import java.util.List;

/** Contrato del servicio de consentimientos informados. */
public interface ConsentService {

    /** Lista todos los consentimientos del tenant (uso del admin). */
    List<ConsentDTO> getAll();

    /** Lista los consentimientos de un paciente específico. */
    List<ConsentDTO> getByPatientId(Long patientId);

    /** Detalle de un consentimiento por ID. */
    ConsentDTO getById(Long id);

    /**
     * Registra la firma de un consentimiento.
     *
     * @param patientId  ID del paciente (tomado del JWT, nunca del body)
     * @param dto        datos de entrada: serviceId + digitalSignature
     * @param ipAddress  IP del cliente para trazabilidad legal
     * @return el consentimiento firmado y persistido
     */
    ConsentDTO sign(Long patientId, ConsentDTO dto, String ipAddress);
}
