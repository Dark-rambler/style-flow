package com.styloflow.users.application.port.in.command;

import com.styloflow.users.domain.enums.Role;
import java.math.BigDecimal;

public record UserCommand(
        String name,
        String username,
        String password,
        Role role,
        String phone,
        BigDecimal commissionRate,
        Boolean active
) {}
