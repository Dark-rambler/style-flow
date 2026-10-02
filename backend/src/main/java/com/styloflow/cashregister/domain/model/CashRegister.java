package com.styloflow.cashregister.domain.model;

import com.styloflow.sales.domain.model.PaymentMethod;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.users.domain.model.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Cash register shift: opened with a float and closed with a cash count. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegister {

    private Long id;
    private CashRegisterStatus status;
    private User openedBy;
    private Instant openedAt;
    private BigDecimal openingAmount;
    private User closedBy;
    private Instant closedAt;
    private BigDecimal expectedCash;
    private BigDecimal countedCash;
    private BigDecimal difference;
    private BigDecimal salesTotal;
    private String notes;

    public static CashRegister open(User by, Instant at, BigDecimal openingAmount, String notes) {
        return CashRegister.builder()
                .status(CashRegisterStatus.OPEN)
                .openedBy(by)
                .openedAt(at)
                .openingAmount(openingAmount)
                .notes(notes)
                .build();
    }

    /** Cash count: compares the counted cash with the expected one (opening float + cash sales). */
    public void close(User by, Instant at, BigDecimal counted, String closingNotes, List<PaymentTotal> byPaymentMethod) {
        if (!isOpen()) {
            throw new BusinessRuleException("The cash register is already closed");
        }
        BigDecimal expected = expectedCash(byPaymentMethod);
        status = CashRegisterStatus.CLOSED;
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

    /** Live while open; once closed, the value recorded at the cash count. */
    public BigDecimal expectedCash(List<PaymentTotal> byPaymentMethod) {
        return isOpen() ? openingAmount.add(PaymentTotal.totalOf(byPaymentMethod, PaymentMethod.CASH)) : expectedCash;
    }

    public boolean isOpen() {
        return status == CashRegisterStatus.OPEN;
    }
}
