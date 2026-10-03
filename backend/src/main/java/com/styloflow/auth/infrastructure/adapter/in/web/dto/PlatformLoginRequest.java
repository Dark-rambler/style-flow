package com.styloflow.auth.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PlatformLoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
