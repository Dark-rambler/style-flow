package com.styloflow.cash.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.enums.PaymentMethod;
import java.math.BigDecimal;

public record PaymentTotalResponse(
        PaymentMethod method,
        long count,
        BigDecimal total
) {}
