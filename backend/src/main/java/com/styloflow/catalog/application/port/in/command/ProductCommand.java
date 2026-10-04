package com.styloflow.catalog.application.port.in.command;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ProductCommand(
        String name,
        String sku,
        BigDecimal price,
        Integer stock,
        Integer minStock,
        Boolean active,
        MultipartFile image
) {}
