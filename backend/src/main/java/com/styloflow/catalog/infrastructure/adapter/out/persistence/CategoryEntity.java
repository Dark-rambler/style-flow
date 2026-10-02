package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categorias")
public class CategoryEntity extends TenantScopedEntity {

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
