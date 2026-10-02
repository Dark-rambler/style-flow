package com.styloflow.catalog.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

/** Positive adds units, negative removes them. */
public record StockAdjustmentRequest(@NotNull Integer quantity) {}
