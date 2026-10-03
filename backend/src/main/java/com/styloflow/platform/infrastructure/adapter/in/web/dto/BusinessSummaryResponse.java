package com.styloflow.platform.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BusinessSummaryResponse(
        Long id,
        String code,
        String name,
        boolean active,
        Instant createdAt,
        long users,
        long sales30d,
        BigDecimal total30d
) {}
