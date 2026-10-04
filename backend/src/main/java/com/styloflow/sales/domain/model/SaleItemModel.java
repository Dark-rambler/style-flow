package com.styloflow.sales.domain.model;

import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.model.Money;
import com.styloflow.users.domain.model.UserModel;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemModel {

    private Long id;
    private ItemType type;
    private Long itemId;
    private UserModel stylist;
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal subtotal;

    public static SaleItemModel create(ItemType type,
                                       Long itemId,
                                       String description,
                                       int quantity,
                                       BigDecimal unitPrice,
                                       BigDecimal discount,
                                       UserModel stylist) {
        var price = Money.of(unitPrice);
        var gross = price.multiply(BigDecimal.valueOf(quantity));
        var lineDiscount = Money.ofOrZero(discount);
        if (lineDiscount.compareTo(gross) > 0)
            throw new BusinessRuleException("The discount of '" + description + "' exceeds its amount");
        return SaleItemModel.builder()
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
