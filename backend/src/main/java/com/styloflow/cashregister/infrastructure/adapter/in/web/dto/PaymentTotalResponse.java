package com.styloflow.cashregister.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.model.PaymentMethod;
import java.math.BigDecimal;

public record PaymentTotalResponse(PaymentMethod method, long count, BigDecimal total) {}
