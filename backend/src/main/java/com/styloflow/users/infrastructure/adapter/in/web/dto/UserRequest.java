package com.styloflow.users.infrastructure.adapter.in.web.dto;

import com.styloflow.users.domain.enums.Role;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UserRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(min = 3, max = 50) @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "only letters, numbers, dot, hyphen and underscore"
        ) String username,
        @Size(min = 6, max = 72) String password,
        @NotNull Role role,
        @Size(max = 30) String phone,
        @DecimalMin("0") @DecimalMax("100") BigDecimal commissionRate,
        Boolean active) {}
