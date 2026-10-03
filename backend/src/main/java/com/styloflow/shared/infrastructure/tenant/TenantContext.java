package com.styloflow.shared.infrastructure.tenant;

import java.util.function.Supplier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Business (tenant) of the current request. Resolution order:
 * <ol>
 *   <li>explicit override through {@link #runAs} (login, sign-ups and internal tasks);</li>
 *   <li>{@code bid} claim of the authenticated JWT;</li>
 *   <li>{@link #NO_TENANT}: matches no business, so no data is visible (fail-closed).</li>
 * </ol>
 * Hibernate binds the tenant when the session opens: to switch it inside a transaction use {@link TenantExecutor}.
 */
public final class TenantContext {

    public static final String CLAIM_BUSINESS_ID = "bid";
    public static final String CLAIM_BUSINESS_CODE = "bcode";
    public static final long NO_TENANT = -1L;

    private static final ThreadLocal<Long> OVERRIDE = new ThreadLocal<>();

    private TenantContext() {}

    public static long current() {
        Long override = OVERRIDE.get();
        if (override != null) {
            return override;
        }
        Long fromToken = businessIdFromToken();
        return fromToken != null ? fromToken : NO_TENANT;
    }

    /** Runs {@code action} with the given business as tenant, restoring the previous one afterward. */
    public static <T> T runAs(long businessId, Supplier<T> action) {
        Long previous = OVERRIDE.get();
        OVERRIDE.set(businessId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                OVERRIDE.remove();
            } else {
                OVERRIDE.set(previous);
            }
        }
    }

    /** Business id of the request JWT, or {@code null} without a business token (e.g. superadmin). */
    public static Long businessIdFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt
                && jwt.getClaim(CLAIM_BUSINESS_ID) instanceof Number id) {
            return id.longValue();
        }
        return null;
    }
}
