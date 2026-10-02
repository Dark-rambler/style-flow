package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.model.PaymentMethod;
import com.styloflow.sales.domain.model.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** {@code cashRegisterOpen}: only sales of the open cash register can be voided. */
public record SaleSummaryResponse(Long id, Instant date, String cashier, Long customerId, String customer,
        BigDecimal total, PaymentMethod paymentMethod, SaleStatus status, boolean cashRegisterOpen) {}
