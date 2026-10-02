package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoidSaleRequest(@NotBlank @Size(max = 250) String reason) {}
