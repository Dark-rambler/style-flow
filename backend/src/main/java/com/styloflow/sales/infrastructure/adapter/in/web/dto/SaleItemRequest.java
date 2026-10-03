package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.enums.ItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaleItemRequest(
        @NotNull ItemType type,
        @NotNull Long itemId,
        @NotNull @Min(1) @Max(999) Integer quantity,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal discount,
        Long stylistId
) {}
