package com.styloflow.reports.domain.model;

import java.math.BigDecimal;

/** Aggregates of the sales of a range: amounts of the completed ones and number of voided ones. */
public record SalesTotals(long count, BigDecimal total, BigDecimal discounts, BigDecimal tax, long voided) {}
