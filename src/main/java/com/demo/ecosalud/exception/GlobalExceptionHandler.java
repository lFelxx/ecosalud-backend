package com.demo.ecosalud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de excepciones de la API REST.
 *
 * <p>Convierte excepciones de dominio en respuestas HTTP estructuradas
 * con un cuerpo JSON consistente que el frontend puede interpretar.</p>
 *
 * <table>
 *   <tr><th>Excepción</th><th>HTTP</th><th>error code</th></tr>
 *   <tr><td>PlanLimitExceededException</td><td>402</td><td>PLAN_LIMIT_EXCEEDED</td></tr>
 * </table>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Límite de plan superado — HTTP 402 Payment Required.
     *
     * <p>El cuerpo incluye suficiente información para que el frontend
     * muestre un banner de upgrade con el número exacto de recursos usados
     * y el límite del plan actual.</p>
     */
    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handlePlanLimitExceeded(PlanLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error",     "PLAN_LIMIT_EXCEEDED");
        body.put("limitType", ex.getLimitType().name());
        body.put("message",   ex.getMessage());
        body.put("current",   ex.getCurrent());
        body.put("max",       ex.getMax());
        body.put("plan",      ex.getPlan());
        body.put("upgradeUrl", "/admin/billing");
        body.put("timestamp", Instant.now().toString());

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }
}
