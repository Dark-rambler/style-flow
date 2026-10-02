package com.styloflow.reports.domain.model;

import com.styloflow.sales.domain.model.PaymentMethod;
import java.math.BigDecimal;

/**
 * Completed sales of one payment method.
 *
 * @param method payment method
 * @param count number of sales
 * @param total amount sold
 */
public record PaymentMethodTotal(PaymentMethod method, long count, BigDecimal total) {}
