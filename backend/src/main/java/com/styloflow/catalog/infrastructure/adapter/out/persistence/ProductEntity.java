package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "productos")
public class ProductEntity extends TenantScopedEntity {

    @Column(name = "nombre", nullable = false)
    private String name;

    private String sku;

    @Column(name = "precio", nullable = false)
    private BigDecimal price;

    @Column(name = "imagen_url")
    private String imageUrl;

    @Column(name = "imagen_public_id")
    private String imagePublicId;

    @Column(nullable = false)
    private int stock;

    @Column(name = "stock_minimo", nullable = false)
    private int minStock;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
