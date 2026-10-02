package com.styloflow.users.application.port.in;

import com.styloflow.users.domain.model.Role;
import java.math.BigDecimal;

/** {@code password} is required on create; on update, blank means "keep the current one". */
public record UserCommand(String name, String username, String password, Role role, String phone,
        BigDecimal commissionRate, Boolean active) {}
