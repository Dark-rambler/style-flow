package com.styloflow.shared.infrastructure.tenant;

import com.styloflow.platform.application.port.in.BusinessStatusUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rejects requests of a suspended business with 403, even while its token is still valid. */
public class ActiveBusinessFilter extends OncePerRequestFilter {

    /** ProblemDetail title the frontend uses to detect a suspended business. */
    public static final String SUSPENDED_TITLE = "Business suspended";

    private final BusinessStatusUseCase businessStatus;

    public ActiveBusinessFilter(BusinessStatusUseCase businessStatus) {
        this.businessStatus = businessStatus;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Long businessId = TenantContext.businessIdFromToken();
        if (businessId != null && !businessStatus.isActive(businessId)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {"title":"%s","status":403,"detail":"This business is suspended. Please contact support."}"""
                    .formatted(SUSPENDED_TITLE));
            return;
        }
        chain.doFilter(request, response);
    }
}
