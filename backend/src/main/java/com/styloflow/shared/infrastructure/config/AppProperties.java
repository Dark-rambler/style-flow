package com.styloflow.shared.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String timeZone, Jwt jwt, Cors cors, Admin admin, Superadmin superadmin) {

    public record Jwt(String secret, long expirationHours, String issuer) {}

    public record Cors(List<String> origins) {}

    /** Admin created in the demo business when it has no users (development and tests). */
    public record Admin(String username, String password, String name) {}

    /** Initial platform superadmin (created when none exists). */
    public record Superadmin(String username, String password, String name) {}
}
