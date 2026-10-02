package com.styloflow.reports.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.model.PaymentMethod;
import java.math.BigDecimal;

public record PaymentMethodTotalResponse(PaymentMethod method, long count, BigDecimal total) {}
