package com.styloflow.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/** Acceso al usuario autenticado a partir de los claims del JWT. */
@Component
public class CurrentUser {

    public Long id() {
        Object uid = jwt().getClaim(TokenService.CLAIM_UID);
        return ((Number) uid).longValue();
    }

    public boolean hasRole(String rol) {
        return jwt().getClaimAsStringList(TokenService.CLAIM_ROLES).contains(rol);
    }

    private static Jwt jwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
