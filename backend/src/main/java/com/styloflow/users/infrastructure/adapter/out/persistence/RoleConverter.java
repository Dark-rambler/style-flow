package com.styloflow.users.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.DbEnumConverter;
import com.styloflow.users.domain.model.Role;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class RoleConverter extends DbEnumConverter<Role> {

    public RoleConverter() {
        super(Map.of(Role.ADMIN, "ADMIN", Role.CASHIER, "CAJERO", Role.STYLIST, "ESTILISTA"));
    }
}
