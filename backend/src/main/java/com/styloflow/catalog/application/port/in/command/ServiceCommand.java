package com.styloflow.catalog.application.port.in.command;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ServiceCommand(
        Long categoryId,
        String name,
        String description,
        Integer durationMinutes,
        BigDecimal price,
        Boolean active,
        MultipartFile image
) {}
