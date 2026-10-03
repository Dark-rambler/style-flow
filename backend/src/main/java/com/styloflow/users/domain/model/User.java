package com.styloflow.users.domain.model;

import java.math.BigDecimal;

import com.styloflow.users.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String name;
    private String username;
    private String passwordHash;
    private Role role;
    private String phone;
    @Builder.Default
    private BigDecimal commissionRate = BigDecimal.ZERO;
    @Builder.Default
    private boolean active = true;

    public boolean isActiveAdmin() {
        return role == Role.ADMIN && active;
    }
}
