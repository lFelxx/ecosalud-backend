package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.RevenueDTO;

/**
 * Servicio de métricas financieras de la plataforma.
 *
 * <p>Calcula MRR, ARR, churn, distribución por plan y crecimiento mensual
 * a partir de los datos en {@code public.tenants}.</p>
 *
 * <p>Solo accesible desde el panel de super-administrador.</p>
 */
public interface RevenueService {

    /**
     * Calcula y retorna todas las métricas de ingresos de la plataforma.
     *
     * @return {@link RevenueDTO} con MRR, ARR, conteos por estado/plan y crecimiento mensual
     */
    RevenueDTO getRevenueSummary();
}
