package com.styloflow.catalogo;

import com.styloflow.common.TenantScopedEntity;
import com.styloflow.common.BusinessException;
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
public class Producto extends TenantScopedEntity {

    @Column(nullable = false)
    private String nombre;

    private String sku;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private int stock;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(nullable = false)
    private boolean activo = true;

    public void descontarStock(int cantidad) {
        if (cantidad > stock) {
            throw new BusinessException("Stock insuficiente para '" + nombre + "' (disponible: " + stock + ")");
        }
        stock -= cantidad;
    }

    public void reponerStock(int cantidad) {
        stock += cantidad;
    }
}
