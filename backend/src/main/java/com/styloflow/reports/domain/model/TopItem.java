package com.styloflow.reports.domain.model;

import java.math.BigDecimal;

/**
 * Best-selling service or product.
 *
 * @param id service or product id
 * @param name name at the time of sale
 * @param quantity units sold
 * @param total amount sold
 */
public record TopItem(Long id, String name, long quantity, BigDecimal total) {}
