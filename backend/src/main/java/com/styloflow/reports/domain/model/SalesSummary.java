package com.styloflow.reports.domain.model;

import com.styloflow.shared.domain.model.DateRange;
import com.styloflow.shared.domain.model.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public record SalesSummary(
        LocalDate from,
        LocalDate to,
        long salesCount,
        BigDecimal salesTotal,
        BigDecimal averageTicket,
        BigDecimal totalDiscounts,
        BigDecimal totalTax,
        long voidedSales,
        List<PaymentMethodTotal> byPaymentMethod
) {
    public static SalesSummary of(DateRange range, SalesTotals totals, List<PaymentMethodTotal> byPaymentMethod) {
        var average = totals.count() == 0 ? Money.ZERO : totals.total().divide(BigDecimal.valueOf(totals.count()), 2, RoundingMode.HALF_UP);
        return new SalesSummary(
                range.from(),
                range.to(),
                totals.count(),
                totals.total(),
                average,
                totals.discounts(),
                totals.tax(),
                totals.voided(),
                byPaymentMethod
        );
    }
}
