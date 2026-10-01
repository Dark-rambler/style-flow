package com.styloflow.tenant;

import java.util.function.Supplier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Negocio (tenant) de la petición actual. Orden de resolución:
 * <ol>
 *   <li>override explícito con {@link #ejecutarComo} (login, altas y tareas internas);</li>
 *   <li>claim {@code nid} del JWT autenticado;</li>
 *   <li>{@link #SIN_TENANT}: no coincide con ningún negocio, así que no se ve ningún dato (fail-closed).</li>
 * </ol>
 * Hibernate fija el tenant al abrir la sesión: use {@code ejecutarComo} fuera de transacciones ya abiertas.
 */
public final class TenantContext {

    public static final String CLAIM_NEGOCIO_ID = "nid";
    public static final String CLAIM_NEGOCIO_CODIGO = "ncod";
    public static final long SIN_TENANT = -1L;

    private static final ThreadLocal<Long> OVERRIDE = new ThreadLocal<>();

    private TenantContext() {}

    public static long actual() {
        Long override = OVERRIDE.get();
        if (override != null) {
            return override;
        }
        Long desdeToken = negocioDelToken();
        return desdeToken != null ? desdeToken : SIN_TENANT;
    }

    /** Ejecuta {@code accion} con el negocio indicado como tenant, restaurando el anterior al terminar. */
    public static <T> T ejecutarComo(long negocioId, Supplier<T> accion) {
        Long anterior = OVERRIDE.get();
        OVERRIDE.set(negocioId);
        try {
            return accion.get();
        } finally {
            if (anterior == null) {
                OVERRIDE.remove();
            } else {
                OVERRIDE.set(anterior);
            }
        }
    }

    /** Id de negocio del JWT de la petición, o {@code null} si no hay token de negocio (p. ej. superadmin). */
    public static Long negocioDelToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            Object nid = jwt.getClaim(CLAIM_NEGOCIO_ID);
            if (nid instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }
}
