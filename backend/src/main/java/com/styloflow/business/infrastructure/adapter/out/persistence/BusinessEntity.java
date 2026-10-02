package com.styloflow.business.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Tenants table: it has no {@code negocio_id} column. */
@Getter
@Setter
@Entity
@Table(name = "negocio")
public class BusinessEntity extends BaseEntity {

    @Column(name = "codigo", nullable = false, unique = true, updatable = false)
    private String code;

    @Column(name = "activo", nullable = false)
    private boolean active = true;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "nit")
    private String taxId;

    @Column(name = "direccion")
    private String address;

    @Column(name = "telefono")
    private String phone;

    @Column(name = "moneda", nullable = false)
    private String currency;

    @Column(name = "simbolo", nullable = false)
    private String currencySymbol;

    @Column(name = "iva_porcentaje", nullable = false)
    private BigDecimal taxRate;

    @Column(name = "mensaje_ticket")
    private String receiptMessage;
}
