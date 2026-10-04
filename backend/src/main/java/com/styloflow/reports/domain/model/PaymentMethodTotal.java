package com.styloflow.reports.domain.model;

import com.styloflow.sales.domain.enums.PaymentMethod;
import java.math.BigDecimal;

public record PaymentMethodTotal(
        PaymentMethod method,
        long count,
        BigDecimal total
) {}
