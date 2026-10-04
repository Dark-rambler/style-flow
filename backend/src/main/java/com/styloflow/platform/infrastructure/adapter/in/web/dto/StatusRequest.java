package com.styloflow.platform.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull Boolean active
) {}
