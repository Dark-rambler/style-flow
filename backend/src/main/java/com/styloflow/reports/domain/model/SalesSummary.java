package com.styloflow.reports.domain.model;

import com.styloflow.shared.domain.model.DateRange;
import com.styloflow.shared.domain.model.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Sales KPIs of a date range.
 *
 * @param from first day
 * @param to last day
 * @param salesCount completed sales
 * @param salesTotal amount sold
 * @param averageTicket salesTotal / salesCount
 * @param totalDiscounts global discounts granted
 * @param totalTax tax included in the sales
 * @param voidedSales voided sales
 * @param byPaymentMethod breakdown by payment method
 */
public record SalesSummary(LocalDate from, LocalDate to, long salesCount, BigDecimal salesTotal,
        BigDecimal averageTicket, BigDecimal totalDiscounts, BigDecimal totalTax, long voidedSales,
        List<PaymentMethodTotal> byPaymentMethod) {

    /**
     * Builds the summary computing the average ticket.
     *
     * @param range reported days
     * @param totals aggregated totals
     * @param byPaymentMethod breakdown by payment method
     * @return the summary
     */
    public static SalesSummary of(DateRange range, SalesTotals totals, List<PaymentMethodTotal> byPaymentMethod) {
        BigDecimal average = totals.count() == 0 ? Money.ZERO
                : totals.total().divide(BigDecimal.valueOf(totals.count()), 2, RoundingMode.HALF_UP);
        return new SalesSummary(range.from(), range.to(), totals.count(), totals.total(), average, totals.discounts(),
                totals.tax(), totals.voided(), byPaymentMethod);
    }
}
