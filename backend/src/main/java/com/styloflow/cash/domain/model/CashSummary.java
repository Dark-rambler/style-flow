package com.styloflow.cash.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record CashSummary(
        Cash cash,
        List<PaymentTotal> byPaymentMethod,
        long salesCount,
        BigDecimal salesTotal,
        BigDecimal expectedCash
) {

    public static CashSummary of(Cash cash, List<PaymentTotal> byPaymentMethod) {
        return new CashSummary(
                cash,
                byPaymentMethod,
                byPaymentMethod.stream().mapToLong(PaymentTotal::count).sum(),
                PaymentTotal.sum(byPaymentMethod),
                cash.expectedCash(byPaymentMethod)
        );
    }
}
