package com.styloflow.business.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record BusinessRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 30) String taxId,
        @Size(max = 200) String address,
        @Size(max = 30) String phone,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank @Size(max = 5) String currencySymbol,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal taxRate,
        @Size(max = 250) String receiptMessage
) {}
