package com.styloflow.ventas;

import com.styloflow.caja.Caja;
import com.styloflow.clientes.Cliente;
import com.styloflow.common.BaseEntity;
import com.styloflow.usuarios.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Venta extends BaseEntity {

    public enum Estado { COMPLETADA, ANULADA }

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cajero_id")
    private Usuario cajero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private BigDecimal descuento;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private BigDecimal iva;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Column(name = "monto_recibido", nullable = false)
    private BigDecimal montoRecibido;

    @Column(nullable = false)
    private BigDecimal cambio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.COMPLETADA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulada_por")
    private Usuario anuladaPor;

    @Column(name = "anulada_en")
    private Instant anuladaEn;

    @Column(name = "motivo_anulacion")
    private String motivoAnulacion;

    private String observaciones;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<VentaItem> items = new ArrayList<>();

    public void agregarItem(VentaItem item) {
        item.setVenta(this);
        items.add(item);
    }
}
