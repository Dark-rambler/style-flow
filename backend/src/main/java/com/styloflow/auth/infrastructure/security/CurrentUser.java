package com.styloflow.auth.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Access to the authenticated user from the JWT claims. */
@Component
public class CurrentUser {

    public Long id() {
        return ((Number) jwt().getClaim(JwtTokenAdapter.CLAIM_UID)).longValue();
    }

    public boolean hasRole(String role) {
        return jwt().getClaimAsStringList(JwtTokenAdapter.CLAIM_ROLES).contains(role);
    }

    private static Jwt jwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
