package com.styloflow.platform.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Business with its activity over the last 30 days, for the platform panel. */
public record BusinessSummary(Long id, String code, String name, boolean active, Instant createdAt, long users,
        long sales30d, BigDecimal total30d) {}
