package com.styloflow.reports.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record StylistTotal(
        Long stylistId,
        String stylist,
        long services,
        BigDecimal total,
        BigDecimal commissionRate,
        BigDecimal commission
) {

    public static StylistTotal of(
            Long stylistId,
            String stylist,
            long services,
            BigDecimal total,
            BigDecimal commissionRate
    ) {
        BigDecimal commission = total.multiply(commissionRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new StylistTotal(stylistId, stylist, services, total, commissionRate, commission);
    }
}
