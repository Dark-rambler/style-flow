package com.styloflow.cashregister.domain.model;

import com.styloflow.sales.domain.model.PaymentMethod;
import com.styloflow.shared.domain.model.Money;
import java.math.BigDecimal;
import java.util.List;

public record PaymentTotal(
        PaymentMethod method,
        long count,
        BigDecimal total
) {

    public static BigDecimal sum(List<PaymentTotal> totals) {
        return Money.sum(totals, PaymentTotal::total);
    }

    public static BigDecimal totalOf(List<PaymentTotal> totals, PaymentMethod method) {
        return totals.stream()
                .filter(t -> t.method() == method)
                .map(PaymentTotal::total)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
