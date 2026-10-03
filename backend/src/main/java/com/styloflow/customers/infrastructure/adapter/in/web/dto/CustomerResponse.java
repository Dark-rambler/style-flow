package com.styloflow.customers.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String email,
        String taxId,
        String notes,
        Instant createdAt
) {}
