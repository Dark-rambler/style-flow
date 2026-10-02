package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.infrastructure.adapter.out.persistence.ProductEntity;
import com.styloflow.catalog.infrastructure.adapter.out.persistence.SalonServiceEntity;
import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Belongs to {@link SaleEntity}: it inherits its business, so it does not extend TenantScopedEntity. */
@Getter
@Setter
@Entity
@Table(name = "venta_items")
public class SaleItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id")
    private SaleEntity sale;

    @Column(name = "tipo", nullable = false)
    private ItemType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private SalonServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estilista_id")
    private UserEntity stylist;

    @Column(name = "descripcion", nullable = false)
    private String description;

    @Column(name = "cantidad", nullable = false)
    private int quantity;

    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "descuento", nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal subtotal;
}
