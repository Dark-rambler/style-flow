package com.styloflow.auth.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String businessCode,
        @NotBlank String username,
        @NotBlank String password
) {}
