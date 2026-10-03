package com.styloflow.shared.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String timeZone,
        Jwt jwt,
        Cors cors,
        Admin admin,
        Superadmin superadmin,
        Cloudinary cloudinary
) {

    public record Jwt(String secret, long expirationHours, String issuer) {}

    public record Cors(List<String> origins) {}

    public record Admin(String username, String password, String name) {}

    public record Superadmin(String username, String password, String name) {}

    public record Cloudinary(String url) {}
}
