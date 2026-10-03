package com.styloflow.auth.domain.model;

import java.time.Instant;

public record AuthToken(
        String value,
        Instant expiresAt
) {}
