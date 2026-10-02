package com.styloflow.reports.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record StylistTotalResponse(Long stylistId, String stylist, long services, BigDecimal total,
        BigDecimal commissionRate, BigDecimal commission) {}
