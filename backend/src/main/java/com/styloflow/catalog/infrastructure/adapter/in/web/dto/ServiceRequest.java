package com.styloflow.catalog.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public record ServiceRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotNull @Min(1) Integer durationMinutes,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        Boolean active,
        MultipartFile image
) {}
