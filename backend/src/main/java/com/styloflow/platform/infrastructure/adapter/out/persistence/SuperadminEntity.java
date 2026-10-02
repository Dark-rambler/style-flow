package com.styloflow.platform.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "superadmins")
public class SuperadminEntity extends BaseEntity {

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
