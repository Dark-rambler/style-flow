package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "servicios")
public class SalonServiceEntity extends TenantScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private CategoryEntity category;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "duracion_min", nullable = false)
    private int durationMinutes;

    @Column(name = "precio", nullable = false)
    private BigDecimal price;

    @Column(name = "activo", nullable = false)
    private boolean active = true;
}
