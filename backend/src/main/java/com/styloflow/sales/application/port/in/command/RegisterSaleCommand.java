package com.styloflow.sales.application.port.in.command;

import com.styloflow.sales.domain.enums.ItemType;
import com.styloflow.sales.domain.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public record RegisterSaleCommand(
        Long customerId,
        List<Item> items,
        BigDecimal discount,
        PaymentMethod paymentMethod,
        BigDecimal amountReceived,
        String notes
) {

    public record Item(
            ItemType type,
            Long itemId,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal discount,
            Long stylistId
    ) {}
}
