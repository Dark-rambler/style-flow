package com.styloflow.reports.domain.model;

import java.math.BigDecimal;

public record TopItem(
        Long id,
        String name,
        long quantity,
        BigDecimal total
) {}
