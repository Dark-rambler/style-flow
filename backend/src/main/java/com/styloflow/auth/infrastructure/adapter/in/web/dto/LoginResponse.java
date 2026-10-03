package com.styloflow.auth.infrastructure.adapter.in.web.dto;

import com.styloflow.users.infrastructure.adapter.in.web.dto.UserResponse;
import java.time.Instant;

public record LoginResponse(
        String token,
        Instant expiresAt,
        UserResponse user,
        BusinessInfo business
) {}
