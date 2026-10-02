package com.styloflow.cashregister.domain.model;

import java.math.BigDecimal;
import java.util.List;

/** Cash register with the totals of its sales (live while still open). */
public record CashRegisterSummary(CashRegister cashRegister, List<PaymentTotal> byPaymentMethod, long salesCount,
        BigDecimal salesTotal, BigDecimal expectedCash) {

    public static CashRegisterSummary of(CashRegister cashRegister, List<PaymentTotal> byPaymentMethod) {
        return new CashRegisterSummary(cashRegister, byPaymentMethod,
                byPaymentMethod.stream().mapToLong(PaymentTotal::count).sum(), PaymentTotal.sum(byPaymentMethod),
                cashRegister.expectedCash(byPaymentMethod));
    }
}
