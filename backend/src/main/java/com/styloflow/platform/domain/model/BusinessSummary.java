package com.styloflow.platform.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record BusinessSummary(
        Long id,
        String code,
        String name,
        boolean active,
        Instant createdAt,
        long users,
        long sales30d,
        BigDecimal total30d
) {}
