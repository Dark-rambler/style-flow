package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.sales.domain.enums.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleResponse(
        Long id,
        Instant date,
        Long cashId,
        String cashier,
        Long customerId,
        String customer,
        String customerTaxId,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal tax,
        PaymentMethod paymentMethod,
        BigDecimal amountReceived,
        BigDecimal change,
        SaleStatus status,
        String voidedBy,
        Instant voidedAt,
        String voidReason,
        String notes,
        List<SaleItemResponse> items
) {}
