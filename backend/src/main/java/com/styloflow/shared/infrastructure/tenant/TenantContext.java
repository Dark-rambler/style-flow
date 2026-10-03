package com.styloflow.shared.infrastructure.tenant;

import java.util.function.Supplier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class TenantContext {

    public static final String CLAIM_BUSINESS_ID = "bid";
    public static final String CLAIM_BUSINESS_CODE = "bcode";
    public static final long NO_TENANT = -1L;

    private static final ThreadLocal<Long> OVERRIDE = new ThreadLocal<>();

    private TenantContext() {}

    public static long current() {
        Long override = OVERRIDE.get();
        if (override != null)
            return override;
        Long fromToken = businessIdFromToken();
        return fromToken != null ? fromToken : NO_TENANT;
    }

    public static <T> T runAs(long businessId, Supplier<T> action) {
        Long previous = OVERRIDE.get();
        OVERRIDE.set(businessId);
        try {
            return action.get();
        } finally {
            if (previous == null)
                OVERRIDE.remove();
            else
                OVERRIDE.set(previous);
        }
    }

    public static Long businessIdFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt && jwt.getClaim(CLAIM_BUSINESS_ID) instanceof Number id)
            return id.longValue();
        return null;
    }
}
