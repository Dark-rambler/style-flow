package com.styloflow.users.infrastructure.adapter.in.web.dto;

import com.styloflow.users.domain.model.Role;
import java.math.BigDecimal;

public record UserResponse(Long id, String name, String username, Role role, String phone,
        BigDecimal commissionRate, boolean active) {}
