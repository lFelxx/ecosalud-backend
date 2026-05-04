package com.demo.ecosalud.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.ecosalud.service.impl.UserDetailsServiceImpl;
import com.demo.ecosalud.util.JwtUtils;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No se proporcionó header de autorización válido");
            filterChain.doFilter(request, response);
            return;
        }

        var jwtToken = authHeader.substring(7);
        String username = null;
        
        try {
            username = jwtUtils.extractUsername(jwtToken);
        } catch (Exception e) {
            log.warn("Error al extraer el username del token: {}", e.getMessage());
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (username != null && authentication == null) {
            log.info("Cargando usuario al contexto: {}", username);
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtils.validateToken(jwtToken, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Usuario autenticado correctamente: {}", username);
                }
            } catch (Exception e) {
                log.error("Usuario no encontrado: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);

    }
}
