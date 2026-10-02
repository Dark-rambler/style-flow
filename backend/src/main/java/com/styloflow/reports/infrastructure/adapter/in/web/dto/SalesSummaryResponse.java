package com.styloflow.reports.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesSummaryResponse(LocalDate from, LocalDate to, long salesCount, BigDecimal salesTotal,
        BigDecimal averageTicket, BigDecimal totalDiscounts, BigDecimal totalTax, long voidedSales,
        List<PaymentMethodTotalResponse> byPaymentMethod) {}
