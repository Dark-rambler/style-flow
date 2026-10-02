package com.styloflow.auth.domain.model;

import java.time.Instant;

/**
 * Signed access token.
 *
 * @param value encoded JWT
 * @param expiresAt instant after which the token is rejected
 */
public record AuthToken(String value, Instant expiresAt) {}
