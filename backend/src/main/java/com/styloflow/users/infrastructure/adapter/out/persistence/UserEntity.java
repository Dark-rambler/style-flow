package com.styloflow.users.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import com.styloflow.users.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class UserEntity extends TenantScopedEntity {

    @Column(name = "nombre", nullable = false)
    private String name;

    /** Unique within the business (index uq_usuarios_negocio_username). */
    @Column(nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "rol", nullable = false)
    private Role role;

    @Column(name = "telefono")
    private String phone;

    @Column(name = "comision_porcentaje", nullable = false)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
