package com.styloflow.reports.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySales(
        LocalDate date,
        long count,
        BigDecimal total
) {}
