package com.styloflow.sales.application.port.in;

import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.sales.domain.model.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;

public record RegisterSaleCommand(Long customerId, List<Item> items, BigDecimal discount, PaymentMethod paymentMethod,
        BigDecimal amountReceived, String notes) {

    /** {@code unitPrice} is optional: when null the catalog price is used. */
    public record Item(ItemType type, Long itemId, Integer quantity, BigDecimal unitPrice, BigDecimal discount,
            Long stylistId) {}
}
