package com.demo.ecosalud.model.dto;

import com.demo.ecosalud.enums.AccountType;
import com.demo.ecosalud.enums.BillingStatus;
import com.demo.ecosalud.enums.SubscriptionPlan;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta con la información pública de un tenant.
 * No expone el schema interno ni datos sensibles de facturación.
 */
@Data
@Builder
public class TenantDTO {

    private Long         id;
    private String       name;
    private String       slug;
    private String       subdomain;
    private String       customDomain;
    private Boolean      domainVerified;
    private String       ownerName;
    private String       ownerEmail;
    private String       phone;
    private String       city;
    private String       country;
    private String       specialty;
    private String       primaryColor;
    private String       logoUrl;
    private SubscriptionPlan plan;
    private AccountType  accountType;
    private BillingStatus billingStatus;
    private Boolean      active;
    private Boolean      schemaInitialized;
    private String       createdAt;
}
