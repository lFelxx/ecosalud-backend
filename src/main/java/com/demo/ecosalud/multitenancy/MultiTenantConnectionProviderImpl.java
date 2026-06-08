package com.demo.ecosalud.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Proveedor de conexiones multi-tenant para Hibernate.
 *
 * <p>Cada vez que Hibernate necesita una conexión para el tenant activo,
 * este proveedor la obtiene del pool y ajusta el {@code search_path}
 * de PostgreSQL para apuntar al schema del tenant.</p>
 *
 * <p>PostgreSQL {@code search_path} example:</p>
 * <pre>
 *   SET search_path TO tenant_fisiosalud, public;
 *   -- Las tablas se buscan primero en tenant_fisiosalud, luego en public
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiTenantConnectionProviderImpl
        implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    // ── Conexión genérica (sin tenant específico) ────────────────────────────

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    // ── Conexión para un tenant específico ───────────────────────────────────

    @Override
    public Connection getConnection(String tenantSchema) throws SQLException {
        Connection conn = getAnyConnection();
        try {
            // Establece el schema del tenant como primera opción de búsqueda.
            // "public" siempre queda como fallback para entidades de plataforma.
            String searchPath = TenantContext.PUBLIC_SCHEMA.equals(tenantSchema)
                    ? TenantContext.PUBLIC_SCHEMA
                    : tenantSchema + ", " + TenantContext.PUBLIC_SCHEMA;

            conn.createStatement().execute("SET search_path TO " + searchPath);
            log.debug("search_path establecido: {}", searchPath);
        } catch (SQLException e) {
            log.error("Error al establecer search_path para tenant '{}': {}", tenantSchema, e.getMessage());
            throw e;
        }
        return conn;
    }

    @Override
    public void releaseConnection(String tenantSchema, Connection connection) throws SQLException {
        try {
            // Restaura al schema público antes de devolver la conexión al pool
            connection.createStatement().execute("SET search_path TO " + TenantContext.PUBLIC_SCHEMA);
        } catch (SQLException e) {
            log.warn("No se pudo restaurar search_path al liberar conexión: {}", e.getMessage());
        } finally {
            releaseAnyConnection(connection);
        }
    }

    // ── Configuración ────────────────────────────────────────────────────────

    @Override
    public boolean supportsAggressiveRelease() {
        // Retorna false: no liberamos conexiones agresivamente
        // ya que necesitamos mantener el search_path durante la transacción
        return false;
    }

    @Override
    public boolean isUnwrappableAs(@SuppressWarnings("rawtypes") Class aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        throw new UnsupportedOperationException("No se puede desempaquetar " + aClass.getName());
    }
}
