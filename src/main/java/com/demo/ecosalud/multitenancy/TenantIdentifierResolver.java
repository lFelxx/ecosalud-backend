package com.demo.ecosalud.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Le dice a Hibernate qué schema usar para cada petición.
 *
 * <p>Lee el valor almacenado en {@link TenantContext} (escrito por
 * {@link TenantFilter}) y lo entrega al {@link MultiTenantConnectionProviderImpl}.</p>
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.get(); // nunca null: devuelve "public" por defecto
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // No re-validamos sesiones existentes para evitar overhead
        return false;
    }
}
