package com.styloflow.cash.domain.model;

import com.styloflow.cash.domain.enums.CashStatus;
import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.users.domain.model.UserModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashModel {

    private Long id;
    private CashStatus status;
    private UserModel openedBy;
    private Instant openedAt;
    private BigDecimal openingAmount;
    private UserModel closedBy;
    private Instant closedAt;
    private BigDecimal expectedCash;
    private BigDecimal countedCash;
    private BigDecimal difference;
    private BigDecimal salesTotal;
    private String notes;

    public static CashModel open(UserModel by, Instant at, BigDecimal openingAmount, String notes) {
        return CashModel.builder()
                .status(CashStatus.OPEN)
                .openedBy(by)
                .openedAt(at)
                .openingAmount(openingAmount)
                .notes(notes)
                .build();
    }

    public void close(UserModel by, Instant at, BigDecimal counted, String closingNotes, List<PaymentTotal> byPaymentMethod) {
        if (!isOpen())
            throw new BusinessRuleException("The cash register is already closed");
        var expected = expectedCash(byPaymentMethod);
        status = CashStatus.CLOSED;
        closedBy = by;
        closedAt = at;
        salesTotal = PaymentTotal.sum(byPaymentMethod);
        expectedCash = expected;
        countedCash = counted;
        difference = counted.subtract(expected);
        if (closingNotes != null && !closingNotes.isBlank()) {
            notes = closingNotes;
        }
    }

    public BigDecimal expectedCash(List<PaymentTotal> byPaymentMethod) {
        return isOpen() ? openingAmount.add(PaymentTotal.totalOf(byPaymentMethod, PaymentMethod.CASH)) : expectedCash;
    }

    public boolean isOpen() {
        return status == CashStatus.OPEN;
    }
}
