package com.demo.ecosalud.multitenancy;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.ecosalud.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro que extrae el tenant del JWT y lo establece en {@link TenantContext}.
 *
 * <p>Se ejecuta en cada petición HTTP, ANTES de que Spring Security
 * procese la autenticación. El tenant se obtiene del claim {@code "tenant"}
 * dentro del JWT.</p>
 *
 * <p>Flujo:</p>
 * <pre>
 *   Authorization: Bearer {jwt}
 *     → extrae claim "tenant" = "tenant_fisiosalud"
 *     → TenantContext.set("tenant_fisiosalud")
 *     → continúa la cadena de filtros
 *     → al finalizar: TenantContext.clear()
 * </pre>
 *
 * <p>Si no hay JWT o no tiene claim de tenant, se usa el schema {@code public}
 * (acceso a recursos de plataforma o endpoints públicos).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String tenantSchema = extractTenantFromRequest(request);
            TenantContext.set(tenantSchema);
            log.debug("[TenantFilter] tenant activo: {}", tenantSchema);
            filterChain.doFilter(request, response);
        } finally {
            // CRÍTICO: siempre limpiar el contexto para evitar fugas entre hilos
            TenantContext.clear();
        }
    }

    /**
     * Extrae el schema del tenant del JWT o del header {@code X-Tenant-Slug}.
     *
     * @param request petición HTTP entrante
     * @return schema del tenant, o {@code "public"} si no se puede determinar
     */
    private String extractTenantFromRequest(HttpServletRequest request) {
        // 1. Intenta leer el claim "tenant" del JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String tenantClaim = jwtUtils.extractClaim(token,
                        claims -> claims.get("tenant", String.class));
                if (tenantClaim != null && !tenantClaim.isBlank()) {
                    return tenantClaim;
                }
            } catch (Exception e) {
                log.debug("No se pudo extraer tenant del JWT: {}", e.getMessage());
            }
        }

        // 2. Fallback: header X-Tenant-Slug (útil para pruebas / dev tools)
        String tenantHeader = request.getHeader("X-Tenant-Slug");
        if (tenantHeader != null && !tenantHeader.isBlank()) {
            return "tenant_" + tenantHeader.toLowerCase().replace("-", "_");
        }

        // 3. Default: schema público (plataforma o endpoints no autenticados)
        return TenantContext.PUBLIC_SCHEMA;
    }
}
