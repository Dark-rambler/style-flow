package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.model.ItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @param itemId id of the service or product, depending on {@code type}
 * @param unitPrice optional: a price different from the catalog one (e.g. long hair)
 * @param discount optional: discount of this line; cannot exceed price × quantity
 */
public record SaleItemRequest(
        @NotNull ItemType type,
        @NotNull Long itemId,
        @NotNull @Min(1) @Max(999) Integer quantity,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal discount,
        Long stylistId) {}
