package com.styloflow.catalog.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record SalonServiceResponse(Long id, Long categoryId, String category, String name, String description,
        int durationMinutes, BigDecimal price, boolean active) {}
