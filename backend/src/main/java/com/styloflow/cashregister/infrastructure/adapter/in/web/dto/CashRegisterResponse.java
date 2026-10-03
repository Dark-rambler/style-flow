package com.styloflow.cashregister.infrastructure.adapter.in.web.dto;

import com.styloflow.cashregister.domain.model.CashRegisterStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CashRegisterResponse(
        Long id,
        CashRegisterStatus status,
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
