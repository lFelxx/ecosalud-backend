package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.BillingStatusDTO;
import com.demo.ecosalud.model.dto.CheckoutRequestDTO;
import com.demo.ecosalud.model.dto.CheckoutResponseDTO;

import java.util.Map;

/**
 * Servicio de facturación — gestiona suscripciones de tenants via PayU Colombia.
 */
public interface BillingService {

    /**
     * Genera los parámetros del formulario PayU Webcheckout para el plan solicitado.
     *
     * @param tenantSchema schema del tenant que quiere suscribirse (desde JWT)
     * @param request      plan y periodicidad solicitados
     * @return parámetros firmados listos para enviar al checkout de PayU
     */
    CheckoutResponseDTO generateCheckout(String tenantSchema, CheckoutRequestDTO request);

    /**
     * Procesa la confirmación de pago enviada por PayU al webhook.
     *
     * <p>Verifica la firma MD5, determina si el pago fue aprobado y actualiza
     * el plan y estado de facturación del tenant en la base de datos.</p>
     *
     * @param params todos los parámetros POST recibidos de PayU
     */
    void processConfirmation(Map<String, String> params);

    /**
     * Devuelve el estado actual de la suscripción del tenant.
     *
     * @param tenantSchema schema del tenant (desde JWT)
     * @return DTO con plan, billing status y precios
     */
    BillingStatusDTO getStatus(String tenantSchema);
}
