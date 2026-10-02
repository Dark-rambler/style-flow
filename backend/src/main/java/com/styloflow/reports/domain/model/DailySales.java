package com.styloflow.reports.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Completed sales of one day.
 *
 * @param date day in the business time zone
 * @param count number of sales
 * @param total amount sold
 */
public record DailySales(LocalDate date, long count, BigDecimal total) {}
