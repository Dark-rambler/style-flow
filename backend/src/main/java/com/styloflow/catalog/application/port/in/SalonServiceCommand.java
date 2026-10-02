package com.styloflow.catalog.application.port.in;

import java.math.BigDecimal;

/**
 * Data to create or update a salon service.
 *
 * @param categoryId category the service belongs to
 * @param name service name
 * @param description optional description
 * @param durationMinutes estimated duration
 * @param price price including tax
 * @param active new active flag, or {@code null} to keep it
 */
public record SalonServiceCommand(Long categoryId, String name, String description, Integer durationMinutes,
        BigDecimal price, Boolean active) {}
