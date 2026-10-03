package com.styloflow.sales.domain.model;

import com.styloflow.cash.domain.model.Cash;
import com.styloflow.customers.domain.model.Customer;
import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.sales.domain.enums.SaleStatus;
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

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private Long id;
    private Instant date;
    private Cash cash;
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

    public static Sale register(Instant date,
                                Cash cash,
                                User cashier,
                                Customer customer,
                                List<SaleItem> items,
                                BigDecimal globalDiscount,
                                PaymentMethod paymentMethod,
                                BigDecimal amountReceived,
                                BigDecimal taxRate,
                                String notes) {
        var subtotal = Money.sum(items, SaleItem::getSubtotal);
        var discount = Money.ofOrZero(globalDiscount);
        if (discount.compareTo(subtotal) > 0)
            throw new BusinessRuleException("The discount cannot exceed the subtotal");
        var total = subtotal.subtract(discount);
        var received = total;
        var change = Money.ZERO;
        if (paymentMethod == PaymentMethod.CASH) {
            received = amountReceived != null ? Money.of(amountReceived) : total;
            if (received.compareTo(total) < 0)
                throw new BusinessRuleException("The amount received is less than the total");
            change = received.subtract(total);
        }
        return Sale.builder()
                .date(date)
                .cash(cash)
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

    public void voidSale(User by, String reason, Instant at) {
        if (status == SaleStatus.VOIDED)
            throw new BusinessRuleException("The sale is already voided");
        if (!cash.isOpen())
            throw new BusinessRuleException("Only sales of the open cash register can be voided");
        status = SaleStatus.VOIDED;
        voidedBy = by;
        voidedAt = at;
        voidReason = reason.trim();
    }

    public static BigDecimal includedTax(BigDecimal total, BigDecimal rate) {
        if (rate.signum() == 0)
            return Money.ZERO;
        return total.multiply(rate).divide(ONE_HUNDRED.add(rate), 2, RoundingMode.HALF_UP);
    }
}
