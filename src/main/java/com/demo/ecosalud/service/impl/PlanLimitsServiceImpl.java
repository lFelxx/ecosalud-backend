package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.RolUser;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.exception.PlanLimitExceededException;
import com.demo.ecosalud.exception.PlanLimitExceededException.LimitType;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.multitenancy.TenantContext;
import com.demo.ecosalud.repository.AppointmentRepository;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.repository.UserRepository;
import com.demo.ecosalud.service.PlanLimitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Implementación de {@link PlanLimitsService}.
 *
 * <h3>Estrategia</h3>
 * <ol>
 *   <li>Obtiene el schema del tenant activo desde {@link TenantContext}.</li>
 *   <li>Resuelve el plan en {@code public.tenants} vía {@code TenantRepository}
 *       (safe: {@code @Table(schema="public")} evita routing multi-tenant).</li>
 *   <li>Si el plan es ilimitado (CLINIC / FOUNDER), retorna sin verificar.</li>
 *   <li>Cuenta recursos en el schema del tenant y compara contra el límite.</li>
 * </ol>
 *
 * <h3>Conteo de citas</h3>
 * Se usa {@code appointmentDate} (fecha de la cita) en lugar de {@code createdAt}
 * para reflejar la capacidad real del mes: cuántas citas ocurren en el mes,
 * no cuándo se crearon en el sistema.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanLimitsServiceImpl implements PlanLimitsService {

    private final TenantRepository      tenantRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository        userRepository;

    // ── Verificación de citas ────────────────────────────────────────────────

    @Override
    public void checkAppointmentLimit() {
        Tenant tenant = resolveTenant();
        if (tenant == null) return; // Super-admin / plataforma — sin límites

        assertBillingNotOverdue(tenant);

        SubscriptionPlan plan = effectivePlan(tenant);
        if (plan.isUnlimited()) return;

        int       max     = plan.maxAppointmentsPerMonth();
        YearMonth ym      = YearMonth.now();
        long      current = appointmentRepository.countByAppointmentDateBetween(ym.atDay(1), ym.atEndOfMonth());

        if (current >= max) {
            log.warn("[PlanLimits] Tenant '{}' — citas: {}/{} (plan {})",
                     TenantContext.get(), current, max, plan);
            throw new PlanLimitExceededException(LimitType.APPOINTMENTS, (int) current, max, plan.name());
        }
    }

    // ── Verificación de pacientes ────────────────────────────────────────────

    @Override
    public void checkPatientLimit() {
        Tenant tenant = resolveTenant();
        if (tenant == null) return;

        assertBillingNotOverdue(tenant);

        SubscriptionPlan plan    = effectivePlan(tenant);
        if (plan.isUnlimited()) return;

        int  max     = plan.maxPatients();
        long current = userRepository.countByRole(RolUser.USER);

        if (current >= max) {
            log.warn("[PlanLimits] Tenant '{}' — pacientes: {}/{} (plan {})",
                     TenantContext.get(), current, max, plan);
            throw new PlanLimitExceededException(LimitType.PATIENTS, (int) current, max, plan.name());
        }
    }

    // ── Helpers internos ─────────────────────────────────────────────────────

    /**
     * Resuelve el tenant del request actual desde {@code public.tenants}.
     * Devuelve {@code null} cuando no hay tenant en contexto (super-admin).
     */
    private Tenant resolveTenant() {
        String schema = TenantContext.get();
        if (schema == null || schema.isBlank()) {
            log.debug("[PlanLimits] Sin tenant en contexto — operación de plataforma, sin límites");
            return null;
        }
        return tenantRepository.findBySchemaName(schema).orElseGet(() -> {
            log.warn("[PlanLimits] Tenant '{}' no encontrado en public.tenants", schema);
            return null;
        });
    }

    /**
     * Lanza {@link PlanLimitExceededException} con tipo {@code TRIAL_EXPIRED}
     * si el tenant está en estado {@code OVERDUE} (trial vencido o pago vencido).
     */
    private void assertBillingNotOverdue(Tenant tenant) {
        BillingStatus status = tenant.getBillingStatus();
        if (status == BillingStatus.OVERDUE) {
            String planName = tenant.getPlan() != null ? tenant.getPlan().name() : "STARTER";
            log.warn("[PlanLimits] Tenant '{}' en OVERDUE — bloqueando creación de recursos", tenant.getSchemaName());
            throw new PlanLimitExceededException(LimitType.TRIAL_EXPIRED, 0, 0, planName);
        }
    }

    /** Retorna el plan efectivo del tenant, defaulteando a STARTER si es nulo. */
    private SubscriptionPlan effectivePlan(Tenant tenant) {
        return tenant.getPlan() != null ? tenant.getPlan() : SubscriptionPlan.STARTER;
    }
}
