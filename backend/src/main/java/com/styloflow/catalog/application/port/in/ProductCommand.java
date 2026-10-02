package com.styloflow.catalog.application.port.in;

import java.math.BigDecimal;

/**
 * Data to create or update a product.
 *
 * @param name product name
 * @param sku optional stock keeping unit (unique per business, stored uppercase)
 * @param price sale price including tax
 * @param stock units in stock
 * @param minStock threshold for the low-stock alert, {@code null} means 0
 * @param active new active flag, or {@code null} to keep it
 */
public record ProductCommand(String name, String sku, BigDecimal price, Integer stock, Integer minStock,
        Boolean active) {}
