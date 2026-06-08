package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.ConsentDTO;
import com.demo.ecosalud.model.entities.Consent;
import com.demo.ecosalud.repository.ConsentRepository;
import com.demo.ecosalud.repository.ServiceRepository;
import com.demo.ecosalud.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de consentimientos informados.
 *
 * <p>Los consentimientos son registros <b>inmutables</b> una vez firmados:
 * no existe método de update ni delete en esta implementación.
 * Esto es un requisito de trazabilidad legal.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository consentRepo;
    private final UserRepository    userRepo;
    private final ServiceRepository serviceRepo;

    // ── Template del consentimiento ───────────────────────────────────────────
    // Se almacena textualmente en el momento de la firma para garantizar
    // que el documento legal sea exactamente lo que el paciente aceptó.

    private static final String CONSENT_TEMPLATE = """
        CONSENTIMIENTO INFORMADO PARA TRATAMIENTO DE TERAPIAS ALTERNATIVAS

        Yo, el/la paciente abajo firmante, declaro que:

        1. He sido informado/a por el especialista de la clínica sobre el procedimiento
           denominado "%s", incluyendo sus beneficios, riesgos y alternativas.

        2. Entiendo que las terapias alternativas son complementarias a la medicina
           convencional y no la sustituyen.

        3. He tenido la oportunidad de realizar preguntas y estas han sido respondidas
           satisfactoriamente.

        4. Doy mi consentimiento libre, voluntario e informado para recibir el tratamiento
           indicado, pudiendo revocarlo en cualquier momento sin perjuicio alguno.

        5. Autorizo el uso de mis datos de salud exclusivamente para los fines del
           tratamiento, conforme a la Ley 1581 de 2012 (Habeas Data — Colombia).

        Este consentimiento fue generado y firmado digitalmente a través de la plataforma
        Ecosalud Market. La firma digital equivale a la firma manuscrita para efectos del
        tratamiento acordado.

        Fecha y hora de firma: %s
        """;

    // ── Implementación ────────────────────────────────────────────────────────

    @Override
    public List<ConsentDTO> getAll() {
        return consentRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ConsentDTO> getByPatientId(Long patientId) {
        return consentRepo.findByPatientIdOrderBySignedAtDesc(patientId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ConsentDTO getById(Long id) {
        return toDTO(consentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consentimiento no encontrado: " + id)));
    }

    @Override
    public ConsentDTO sign(Long patientId, ConsentDTO dto, String ipAddress) {
        // Resolver nombre del servicio para el template
        String serviceName = dto.getServiceId() != null
                ? serviceRepo.findById(dto.getServiceId())
                             .map(s -> s.getName())
                             .orElse("Terapia general")
                : "Terapia general";

        LocalDateTime now = LocalDateTime.now();
        String consentText = CONSENT_TEMPLATE.formatted(
                serviceName,
                now.toString().replace("T", " ").substring(0, 19)
        );

        Consent c = new Consent();
        c.setPatientId(patientId);
        c.setServiceId(dto.getServiceId());
        c.setConsentText(consentText);
        c.setDigitalSignature(dto.getDigitalSignature());
        c.setIpAddress(ipAddress);
        c.setSignedAt(now);
        c.setCreatedAt(now);

        Consent saved = consentRepo.save(c);
        log.info("[Consent] Paciente {} firmó consentimiento para servicio '{}' desde IP {}",
                patientId, serviceName, ipAddress);

        return toDTO(saved);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ConsentDTO toDTO(Consent c) {
        ConsentDTO dto = new ConsentDTO();
        dto.setId(c.getId());
        dto.setPatientId(c.getPatientId());
        dto.setServiceId(c.getServiceId());
        dto.setConsentText(c.getConsentText());
        dto.setDigitalSignature(c.getDigitalSignature());
        dto.setIpAddress(c.getIpAddress());
        dto.setSignedAt(c.getSignedAt());
        dto.setCreatedAt(c.getCreatedAt());

        // Resolver nombres
        if (c.getPatientId() != null) {
            userRepo.findById(c.getPatientId()).ifPresent(u -> {
                dto.setPatientName(u.getName());
                dto.setPatientEmail(u.getEmail());
            });
        }
        if (c.getServiceId() != null) {
            serviceRepo.findById(c.getServiceId())
                    .ifPresent(s -> dto.setServiceName(s.getName()));
        }
        return dto;
    }
}
