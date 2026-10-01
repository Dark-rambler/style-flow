package com.styloflow.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String zonaHoraria, Jwt jwt, Cors cors, Admin admin, Superadmin superadmin) {

    public record Jwt(String secret, long expirationHours, String issuer) {}

    public record Cors(List<String> origins) {}

    /** Admin que se crea en el negocio demo si no tiene usuarios (desarrollo y tests). */
    public record Admin(String username, String password, String nombre) {}

    /** Superadmin inicial de la plataforma (se crea si no existe ninguno). */
    public record Superadmin(String username, String password, String nombre) {}
}
