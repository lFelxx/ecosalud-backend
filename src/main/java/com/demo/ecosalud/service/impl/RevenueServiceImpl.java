package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;
import com.demo.ecosalud.model.dto.RevenueDTO;
import com.demo.ecosalud.model.dto.RevenueDTO.MonthlyGrowth;
import com.demo.ecosalud.model.entities.Tenant;
import com.demo.ecosalud.repository.TenantRepository;
import com.demo.ecosalud.service.RevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de {@link RevenueService}.
 *
 * <h3>Precios COP (mensual)</h3>
 * <ul>
 *   <li>STARTER → $119.000</li>
 *   <li>PRO     → $324.000</li>
 *   <li>CLINIC  → $817.000</li>
 *   <li>FOUNDER → $0 (exento)</li>
 * </ul>
 *
 * <h3>MRR</h3>
 * Solo se suma el precio de tenants con {@code billingStatus = ACTIVE}.
 * Los tenants en TRIAL no generan MRR aún; los OVERDUE tampoco.
 *
 * <h3>Crecimiento mensual</h3>
 * Se agrupa por mes de {@code createdAt} los últimos 6 meses.
 * El MRR del mes refleja cuánto pagan actualmente los tenants
 * creados ese mes (solo los que llegaron a ACTIVE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevenueServiceImpl implements RevenueService {

    private final TenantRepository tenantRepository;

    // Precios mensuales en COP por plan
    private static final Map<SubscriptionPlan, Long> PRICE_COP = Map.of(
        SubscriptionPlan.STARTER, 119_000L,
        SubscriptionPlan.PRO,     324_000L,
        SubscriptionPlan.CLINIC,  817_000L,
        SubscriptionPlan.FOUNDER,       0L
    );

    private static final DateTimeFormatter MONTH_FMT =
        DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "CO"));

    @Override
    public RevenueDTO getRevenueSummary() {
        List<Tenant> all = tenantRepository.findAll().stream()
            .filter(t -> Boolean.TRUE.equals(t.getActive()))
            .toList();

        // ── Conteos por estado ────────────────────────────────────────────────

        int activeTenants  = count(all, BillingStatus.ACTIVE);
        int trialTenants   = count(all, BillingStatus.TRIAL);
        int overdueTenants = count(all, BillingStatus.OVERDUE);
        int exemptTenants  = count(all, BillingStatus.EXEMPT);
        int totalTenants   = all.size();

        // ── Distribución por plan (solo ACTIVE) ──────────────────────────────

        int starterActive = countPlanActive(all, SubscriptionPlan.STARTER);
        int proActive     = countPlanActive(all, SubscriptionPlan.PRO);
        int clinicActive  = countPlanActive(all, SubscriptionPlan.CLINIC);
        int founderCount  = (int) all.stream()
            .filter(t -> t.getPlan() == SubscriptionPlan.FOUNDER).count();

        // ── MRR (solo ACTIVE) ─────────────────────────────────────────────────

        long mrrCOP = all.stream()
            .filter(t -> t.getBillingStatus() == BillingStatus.ACTIVE)
            .mapToLong(t -> PRICE_COP.getOrDefault(t.getPlan(), 0L))
            .sum();

        long arrCOP = mrrCOP * 12;

        // ── MRR potencial si todos los TRIAL convirtieran ─────────────────────

        long potentialMrrCOP = mrrCOP + all.stream()
            .filter(t -> t.getBillingStatus() == BillingStatus.TRIAL)
            .mapToLong(t -> PRICE_COP.getOrDefault(t.getPlan(), PRICE_COP.get(SubscriptionPlan.STARTER)))
            .sum();

        // ── Tasa de conversión ────────────────────────────────────────────────
        // % de tenants que terminaron el trial y pagaron (vs. los que vencieron)

        double conversionRate = 0.0;
        int denominator = activeTenants + overdueTenants;
        if (denominator > 0) {
            conversionRate = Math.round((activeTenants * 100.0 / denominator) * 10.0) / 10.0;
        }

        // ── Crecimiento mensual (últimos 6 meses) ────────────────────────────

        List<MonthlyGrowth> last6Months = buildMonthlyGrowth(all);

        log.debug("[Revenue] MRR={} ARR={} active={} trial={} overdue={} conversion={}%",
            mrrCOP, arrCOP, activeTenants, trialTenants, overdueTenants, conversionRate);

        return RevenueDTO.builder()
            .mrrCOP(mrrCOP)
            .arrCOP(arrCOP)
            .potentialMrrCOP(potentialMrrCOP)
            .activeTenants(activeTenants)
            .trialTenants(trialTenants)
            .overdueTenants(overdueTenants)
            .exemptTenants(exemptTenants)
            .totalTenants(totalTenants)
            .starterActive(starterActive)
            .proActive(proActive)
            .clinicActive(clinicActive)
            .founderCount(founderCount)
            .conversionRate(conversionRate)
            .last6Months(last6Months)
            .build();
    }

    // ── Helpers de cálculo ───────────────────────────────────────────────────

    private int count(List<Tenant> tenants, BillingStatus status) {
        return (int) tenants.stream()
            .filter(t -> t.getBillingStatus() == status)
            .count();
    }

    private int countPlanActive(List<Tenant> tenants, SubscriptionPlan plan) {
        return (int) tenants.stream()
            .filter(t -> t.getBillingStatus() == BillingStatus.ACTIVE && t.getPlan() == plan)
            .count();
    }

    /**
     * Construye la lista de crecimiento mensual para los últimos 6 meses.
     * Por cada mes calcula:
     * <ul>
     *   <li>{@code newClinics} — tenants creados ese mes</li>
     *   <li>{@code mrrCOP} — ingresos que generan actualmente los tenants
     *       creados ese mes que llegaron a ACTIVE</li>
     * </ul>
     */
    private List<MonthlyGrowth> buildMonthlyGrowth(List<Tenant> allTenants) {
        List<MonthlyGrowth> result = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end   = ym.atEndOfMonth().atTime(23, 59, 59);

            List<Tenant> inMonth = allTenants.stream()
                .filter(t -> t.getCreatedAt() != null
                    && !t.getCreatedAt().isBefore(start)
                    && !t.getCreatedAt().isAfter(end))
                .toList();

            int  newClinics = inMonth.size();
            long mrrMonth   = inMonth.stream()
                .filter(t -> t.getBillingStatus() == BillingStatus.ACTIVE)
                .mapToLong(t -> PRICE_COP.getOrDefault(t.getPlan(), 0L))
                .sum();

            result.add(MonthlyGrowth.builder()
                .month(ym.atDay(1).format(MONTH_FMT))
                .newClinics(newClinics)
                .mrrCOP(mrrMonth)
                .build());
        }
        return result;
    }
}
