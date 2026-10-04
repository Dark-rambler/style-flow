package com.styloflow.reports.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesResponse(
        LocalDate date,
        long count,
        BigDecimal total
) {}
