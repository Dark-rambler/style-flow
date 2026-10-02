package com.styloflow.sales.domain.model;

import com.styloflow.cashregister.domain.model.CashRegister;
import com.styloflow.customers.domain.model.Customer;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.model.Money;
import com.styloflow.shared.domain.model.TextUtils;
import com.styloflow.users.domain.model.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POS sale. {@code subtotal} is the sum of the net line amounts and {@code discount} is only the global discount,
 * so the commission of each service is not affected by courtesies on other items. Prices include tax.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private Long id;
    private Instant date;
    private CashRegister cashRegister;
    private User cashier;
    private Customer customer;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private BigDecimal tax;
    private PaymentMethod paymentMethod;
    private BigDecimal amountReceived;
    private BigDecimal change;
    @Builder.Default
    private SaleStatus status = SaleStatus.COMPLETED;
    private User voidedBy;
    private Instant voidedAt;
    private String voidReason;
    private String notes;
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    /** Computes totals, included tax and change; validates the global discount and the amount received. */
    public static Sale register(Instant date, CashRegister cashRegister, User cashier, Customer customer,
            List<SaleItem> items, BigDecimal globalDiscount, PaymentMethod paymentMethod, BigDecimal amountReceived,
            BigDecimal taxRate, String notes) {
        BigDecimal subtotal = Money.sum(items, SaleItem::getSubtotal);
        BigDecimal discount = Money.ofOrZero(globalDiscount);
        if (discount.compareTo(subtotal) > 0) {
            throw new BusinessRuleException("The discount cannot exceed the subtotal");
        }
        BigDecimal total = subtotal.subtract(discount);

        BigDecimal received = total;
        BigDecimal change = Money.ZERO;
        if (paymentMethod == PaymentMethod.CASH) {
            received = amountReceived != null ? Money.of(amountReceived) : total;
            if (received.compareTo(total) < 0) {
                throw new BusinessRuleException("The amount received is less than the total");
            }
            change = received.subtract(total);
        }

        return Sale.builder()
                .date(date)
                .cashRegister(cashRegister)
                .cashier(cashier)
                .customer(customer)
                .items(new ArrayList<>(items))
                .subtotal(subtotal)
                .discount(discount)
                .total(total)
                .tax(includedTax(total, taxRate))
                .paymentMethod(paymentMethod)
                .amountReceived(received)
                .change(change)
                .notes(TextUtils.blankToNull(notes))
                .build();
    }

    /** Only completed sales of the still-open cash register (so the cash count does not change after closing). */
    public void voidSale(User by, String reason, Instant at) {
        if (status == SaleStatus.VOIDED) {
            throw new BusinessRuleException("The sale is already voided");
        }
        if (!cashRegister.isOpen()) {
            throw new BusinessRuleException("Only sales of the open cash register can be voided");
        }
        status = SaleStatus.VOIDED;
        voidedBy = by;
        voidedAt = at;
        voidReason = reason.trim();
    }

    /** Tax contained in a price that already includes it: total * rate / (100 + rate). */
    public static BigDecimal includedTax(BigDecimal total, BigDecimal rate) {
        if (rate.signum() == 0) {
            return Money.ZERO;
        }
        return total.multiply(rate).divide(ONE_HUNDRED.add(rate), 2, RoundingMode.HALF_UP);
    }
}
