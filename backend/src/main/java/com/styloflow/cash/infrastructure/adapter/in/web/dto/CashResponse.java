package com.styloflow.cash.infrastructure.adapter.in.web.dto;

import com.styloflow.cash.domain.model.CashStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CashResponse(
        Long id,
        CashStatus status,
        String openedBy,
        Instant openedAt,
        BigDecimal openingAmount,
        String closedBy,
        Instant closedAt,
        long salesCount,
        BigDecimal salesTotal,
        BigDecimal expectedCash,
        BigDecimal countedCash,
        BigDecimal difference,
        List<PaymentTotalResponse> byPaymentMethod,
        String notes
) {}
