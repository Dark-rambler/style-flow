package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.cash.infrastructure.adapter.out.persistence.CashEntity;
import com.styloflow.customers.infrastructure.adapter.out.persistence.CustomerEntity;
import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.sales.domain.enums.SaleStatus;
import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import com.styloflow.users.infrastructure.adapter.out.persistence.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ventas")
public class SaleEntity extends TenantScopedEntity {

    @Column(name = "fecha", nullable = false)
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caja_id")
    private CashEntity cash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cajero_id")
    private UserEntity cashier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private CustomerEntity customer;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(name = "descuento", nullable = false)
    private BigDecimal discount;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(name = "iva", nullable = false)
    private BigDecimal tax;

    @Column(name = "metodo_pago", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "monto_recibido", nullable = false)
    private BigDecimal amountReceived;

    @Column(name = "cambio", nullable = false)
    private BigDecimal change;

    @Column(name = "estado", nullable = false)
    private SaleStatus status = SaleStatus.COMPLETED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulada_por")
    private UserEntity voidedBy;

    @Column(name = "anulada_en")
    private Instant voidedAt;

    @Column(name = "motivo_anulacion")
    private String voidReason;

    @Column(name = "observaciones")
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<SaleItemEntity> items = new ArrayList<>();

    public void addItem(SaleItemEntity item) {
        item.setSale(this);
        items.add(item);
    }
}
