package com.demo.ecosalud.multitenancy;

/**
 * Contenedor ThreadLocal del identificador del tenant activo.
 *
 * <p>El schema de PostgreSQL se establece por petición HTTP.
 * El {@link TenantFilter} lo escribe al inicio y lo limpia al final.</p>
 *
 * <p>Uso:</p>
 * <pre>
 *   TenantContext.set("tenant_fisiosalud");
 *   // ... operaciones JPA van al schema tenant_fisiosalud
 *   TenantContext.clear();
 * </pre>
 */
public final class TenantContext {

    /** Schema utilizado cuando no hay tenant en contexto (plataforma). */
    public static final String PUBLIC_SCHEMA = "public";

    private static final ThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>();

    private TenantContext() { /* utilidad estática */ }

    /** Establece el tenant para el hilo actual. */
    public static void set(String tenantSchema) {
        CURRENT_TENANT.set(tenantSchema);
    }

    /** Retorna el schema del tenant activo, o {@code "public"} si no hay ninguno. */
    public static String get() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : PUBLIC_SCHEMA;
    }

    /** Limpia el contexto del hilo — debe llamarse al terminar cada petición. */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /** {@code true} si hay un tenant distinto de public establecido. */
    public static boolean hasTenant() {
        String t = CURRENT_TENANT.get();
        return t != null && !t.isBlank() && !t.equals(PUBLIC_SCHEMA);
    }
}
