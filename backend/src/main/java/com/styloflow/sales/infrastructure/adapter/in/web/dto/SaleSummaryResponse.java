package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.sales.domain.enums.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record SaleSummaryResponse(
        Long id,
        Instant date,
        String cashier,
        Long customerId,
        String customer,
        BigDecimal total,
        PaymentMethod paymentMethod,
        SaleStatus status,
        boolean cashOpen
) {}
