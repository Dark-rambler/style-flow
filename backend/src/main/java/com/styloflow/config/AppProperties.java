package com.styloflow.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String zonaHoraria, Jwt jwt, Cors cors, Admin admin) {

    public record Jwt(String secret, long expirationHours, String issuer) {}

    public record Cors(List<String> origins) {}

    public record Admin(String username, String password, String nombre) {}
}
