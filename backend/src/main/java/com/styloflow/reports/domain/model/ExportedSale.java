package com.styloflow.reports.domain.model;

import com.styloflow.sales.domain.model.PaymentMethod;
import com.styloflow.sales.domain.model.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** One row of the sales CSV. */
public record ExportedSale(Long id, Instant date, String cashier, String customer, BigDecimal subtotal,
        BigDecimal discount, BigDecimal total, BigDecimal tax, PaymentMethod paymentMethod, SaleStatus status) {}
