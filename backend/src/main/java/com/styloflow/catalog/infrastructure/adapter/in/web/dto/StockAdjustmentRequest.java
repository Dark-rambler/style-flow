package com.styloflow.catalog.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull Integer quantity
) {}
