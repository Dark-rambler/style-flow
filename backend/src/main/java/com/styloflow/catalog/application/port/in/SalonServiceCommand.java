package com.styloflow.catalog.application.port.in;

import org.springframework.web.multipart.MultipartFile;

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
 * @param image optional image bytes, only used on create
 */
public record SalonServiceCommand(Long categoryId, String name, String description, Integer durationMinutes,
        BigDecimal price, Boolean active, MultipartFile image) {}
