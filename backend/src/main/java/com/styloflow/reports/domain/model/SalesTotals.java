package com.styloflow.reports.domain.model;

import java.math.BigDecimal;

public record SalesTotals(
        long count,
        BigDecimal total,
        BigDecimal discounts,
        BigDecimal tax,
        long voided
) {}
