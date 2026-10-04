package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record SaleRequest(
        Long customerId,
        @NotEmpty @Size(max = 50) List<@Valid SaleItemRequest> items,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal discount,
        @NotNull PaymentMethod paymentMethod,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal amountReceived,
        @Size(max = 500) String notes
) {}
