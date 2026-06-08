package com.demo.ecosalud.scheduler;

import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Job programado que gestiona el ciclo de vida de los períodos de prueba.
 *
 * <h3>Dos tareas:</h3>
 * <ol>
 *   <li><b>Aviso previo</b> (09:00 AM diariamente) — detecta trials que vencen en los próximos
 *       3 días y envía un email de advertencia al propietario.</li>
 *   <li><b>Expiración</b> (02:00 AM diariamente) — detecta trials vencidos, los pasa a
 *       {@code OVERDUE} y notifica al propietario.</li>
 * </ol>
 *
 * <h3>Configuración</h3>
 * <pre>
 *   ecosalud.trial.duration-days=14          # Duración del trial en días (default: 14)
 *   ecosalud.trial.expiry-cron=0 0 2 * * *   # Cron de expiración (default: 02:00 AM)
 *   ecosalud.trial.warning-cron=0 0 9 * * *  # Cron de aviso     (default: 09:00 AM)
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrialExpirationJob {

    private final TenantRepository tenantRepository;
    private final EmailService     emailService;

    @Value("${ecosalud.trial.duration-days:14}")
    private int trialDurationDays;

    // ── Aviso previo (3 días antes del vencimiento) ──────────────────────────

    /**
     * Corre cada día a las 09:00 AM.
     * Detecta tenants cuyo trial vencerá en los próximos 3 días (exactamente)
     * y envía un email de advertencia con enlace a la página de suscripción.
     */
    @Scheduled(cron = "${ecosalud.trial.warning-cron:0 0 9 * * *}")
    public void warnExpiringTrials() {
        // Rango: tenants creados entre (ahora - trialDays) y (ahora - trialDays + 3 días)
        // Ej. trial = 14 días → creados entre hace 11 y 14 días (les quedan 0-3 días)
        LocalDateTime warningFrom = LocalDateTime.now().minusDays(trialDurationDays);
        LocalDateTime warningTo   = LocalDateTime.now().minusDays(trialDurationDays - 3);

        List<Tenant> expiring = tenantRepository
                .findByBillingStatusAndCreatedAtBetweenAndActiveTrue(
                        BillingStatus.TRIAL, warningFrom, warningTo);

        if (expiring.isEmpty()) {
            log.debug("[TrialJob] Sin trials próximos a vencer.");
            return;
        }

        log.info("[TrialJob] {} tenant(s) con trial por vencer — enviando aviso.", expiring.size());

        for (Tenant tenant : expiring) {
            long daysUsed = ChronoUnit.DAYS.between(tenant.getCreatedAt(), LocalDateTime.now());
            int  daysLeft = Math.max(1, trialDurationDays - (int) daysUsed);

            try {
                emailService.sendTrialExpiring(
                        tenant.getOwnerEmail(),
                        tenant.getOwnerName(),
                        tenant.getName(),
                        daysLeft,
                        "https://" + tenant.getSubdomain() + "/admin/billing"
                );
                log.info("[TrialJob] Aviso enviado a {} ({} días restantes)", tenant.getOwnerEmail(), daysLeft);
            } catch (Exception e) {
                log.warn("[TrialJob] No se pudo enviar aviso a {}: {}", tenant.getOwnerEmail(), e.getMessage());
            }
        }
    }

    // ── Expiración del trial ─────────────────────────────────────────────────

    /**
     * Corre cada día a las 02:00 AM.
     * Detecta tenants cuyo trial ya expiró (más de {@code trialDurationDays} días
     * desde {@code created_at}), cambia su estado a {@code OVERDUE} y envía
     * el email de notificación.
     *
     * <p>Es idempotente: solo procesa tenants que aún estén en {@code TRIAL}
     * (los ya marcados como OVERDUE no se procesan de nuevo).</p>
     */
    @Scheduled(cron = "${ecosalud.trial.expiry-cron:0 0 2 * * *}")
    @Transactional
    public void expireTrials() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(trialDurationDays);

        List<Tenant> expired = tenantRepository
                .findByBillingStatusAndCreatedAtBeforeAndActiveTrue(BillingStatus.TRIAL, cutoff);

        if (expired.isEmpty()) {
            log.debug("[TrialJob] Sin trials vencidos.");
            return;
        }

        log.info("[TrialJob] {} tenant(s) con trial vencido — marcando como OVERDUE.", expired.size());

        for (Tenant tenant : expired) {
            // Cambiar estado
            tenant.setBillingStatus(BillingStatus.OVERDUE);
            tenantRepository.save(tenant);

            log.info("[TrialJob] Trial expirado: slug='{}' owner='{}' createdAt={}",
                     tenant.getSlug(), tenant.getOwnerEmail(), tenant.getCreatedAt());

            // Notificar al propietario
            try {
                emailService.sendTrialExpired(
                        tenant.getOwnerEmail(),
                        tenant.getOwnerName(),
                        tenant.getName(),
                        "https://" + tenant.getSubdomain() + "/admin/billing"
                );
                log.info("[TrialJob] Email de expiración enviado a {}", tenant.getOwnerEmail());
            } catch (Exception e) {
                // El email no es crítico — el estado ya fue actualizado en BD
                log.warn("[TrialJob] No se pudo enviar email a {}: {}", tenant.getOwnerEmail(), e.getMessage());
            }
        }

        log.info("[TrialJob] Expiración completada. {} tenants actualizados a OVERDUE.", expired.size());
    }
}
