package com.styloflow.reports.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record TopItemResponse(
        Long id,
        String name,
        long quantity,
        BigDecimal total
) {}
