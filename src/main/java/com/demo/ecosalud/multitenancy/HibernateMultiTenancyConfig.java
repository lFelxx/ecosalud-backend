package com.demo.ecosalud.multitenancy;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import lombok.RequiredArgsConstructor;

/**
 * Configuración de JPA/Hibernate con soporte multi-tenant por schema.
 *
 * <p>Registra el {@link MultiTenantConnectionProviderImpl} y el
 * {@link TenantIdentifierResolver} como proveedores de Hibernate,
 * activando la estrategia {@code SCHEMA} de multi-tenancy.</p>
 *
 * <p>Cuando Hibernate ejecuta una query, el flujo es:</p>
 * <pre>
 *   HTTP Request
 *     → TenantFilter extrae el slug del JWT
 *     → TenantContext.set("tenant_fisiosalud")
 *     → TenantIdentifierResolver.resolveCurrentTenantIdentifier()
 *           retorna "tenant_fisiosalud"
 *     → MultiTenantConnectionProviderImpl.getConnection("tenant_fisiosalud")
 *           SET search_path TO tenant_fisiosalud, public
 *     → Hibernate ejecuta queries en ese schema
 *   HTTP Response
 *     → TenantFilter llama TenantContext.clear()
 * </pre>
 */
@Configuration
@RequiredArgsConstructor
public class HibernateMultiTenancyConfig {

    private final MultiTenantConnectionProviderImpl connectionProvider;
    private final TenantIdentifierResolver tenantResolver;
    private final JpaProperties jpaProperties;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.demo.ecosalud.model.entities");

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Combina las propiedades del application.properties con las de multi-tenancy
        Map<String, Object> props = new java.util.HashMap<>(jpaProperties.getProperties());

        // ── Multi-tenancy configuration ──────────────────────────────────────
        props.put("hibernate.multiTenancy", "SCHEMA");
        props.put("hibernate.multi_tenant_connection_provider", connectionProvider);
        props.put("hibernate.tenant_identifier_resolver", tenantResolver);

        // ── Otras propiedades Hibernate ──────────────────────────────────────
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.show_sql", false);
        props.put("hibernate.format_sql", false);

        em.setJpaPropertyMap(props);
        return em;
    }
}
