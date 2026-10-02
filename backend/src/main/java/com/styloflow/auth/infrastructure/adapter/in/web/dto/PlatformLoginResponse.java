package com.styloflow.auth.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record PlatformLoginResponse(String token, Instant expiresAt, String name) {}
