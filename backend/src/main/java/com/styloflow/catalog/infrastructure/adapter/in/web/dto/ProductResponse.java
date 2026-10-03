package com.styloflow.catalog.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, String sku, BigDecimal price, int stock, int minStock,
        boolean active, boolean lowStock, String imageUrl) {}
