package com.styloflow.users.infrastructure.adapter.in.web.dto;

import com.styloflow.users.domain.enums.Role;

public record UserSummaryResponse(
        Long id,
        String name,
        Role role
) {}
