package com.styloflow.cashregister.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.domain.model.CashRegisterStatus;
import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cajas")
public class CashRegisterEntity extends TenantScopedEntity {

    @Column(name = "estado", nullable = false)
    private CashRegisterStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abierta_por")
    private UserEntity openedBy;

    @Column(name = "abierta_en", nullable = false)
    private Instant openedAt;

    @Column(name = "monto_inicial", nullable = false)
    private BigDecimal openingAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cerrada_por")
    private UserEntity closedBy;

    @Column(name = "cerrada_en")
    private Instant closedAt;

    @Column(name = "efectivo_esperado")
    private BigDecimal expectedCash;

    @Column(name = "efectivo_contado")
    private BigDecimal countedCash;

    @Column(name = "diferencia")
    private BigDecimal difference;

    @Column(name = "total_ventas")
    private BigDecimal salesTotal;

    @Column(name = "observaciones")
    private String notes;
}
