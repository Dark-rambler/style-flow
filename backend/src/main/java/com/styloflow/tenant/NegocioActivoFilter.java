package com.styloflow.tenant;

import com.styloflow.plataforma.NegocioEstadoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rechaza con 403 las peticiones de un negocio suspendido, aunque su token siga vigente. */
public class NegocioActivoFilter extends OncePerRequestFilter {

    private final NegocioEstadoService estado;

    public NegocioActivoFilter(NegocioEstadoService estado) {
        this.estado = estado;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Long negocioId = TenantContext.negocioDelToken();
        if (negocioId != null && !estado.activo(negocioId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"title":"Negocio suspendido","status":403,"detail":"Este negocio está suspendido. Contacte al soporte."}""");
            return;
        }
        chain.doFilter(request, response);
    }
}
