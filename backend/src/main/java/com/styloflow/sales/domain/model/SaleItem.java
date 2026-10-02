package com.styloflow.sales.domain.model;

import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.model.Money;
import com.styloflow.users.domain.model.User;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {

    private Long id;
    private ItemType type;
    /** Id of the service or product, depending on {@link #type}. */
    private Long itemId;
    private User stylist;
    /** Name of the service/product at the time of the sale (does not change if the catalog changes). */
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    /** Line discount (courtesy or one-off markdown). */
    private BigDecimal discount;
    /** Net amount of the line: price × quantity − discount. */
    private BigDecimal subtotal;

    public static SaleItem create(ItemType type, Long itemId, String description, int quantity, BigDecimal unitPrice,
            BigDecimal discount, User stylist) {
        BigDecimal price = Money.of(unitPrice);
        BigDecimal gross = price.multiply(BigDecimal.valueOf(quantity));
        BigDecimal lineDiscount = Money.ofOrZero(discount);
        if (lineDiscount.compareTo(gross) > 0) {
            throw new BusinessRuleException("The discount of '" + description + "' exceeds its amount");
        }
        return SaleItem.builder()
                .type(type)
                .itemId(itemId)
                .stylist(stylist)
                .description(description)
                .quantity(quantity)
                .unitPrice(price)
                .discount(lineDiscount)
                .subtotal(gross.subtract(lineDiscount))
                .build();
    }
}
