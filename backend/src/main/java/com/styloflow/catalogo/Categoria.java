package com.styloflow.catalogo;

import com.styloflow.common.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "categorias")
public class Categoria extends TenantScopedEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean activo = true;
}
