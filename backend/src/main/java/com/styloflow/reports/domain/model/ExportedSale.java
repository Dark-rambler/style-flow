package com.styloflow.reports.domain.model;

import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.sales.domain.enums.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ExportedSale(
        Long id,
        Instant date,
        String cashier,
        String customer,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal tax,
        PaymentMethod paymentMethod,
        SaleStatus status
) {}
