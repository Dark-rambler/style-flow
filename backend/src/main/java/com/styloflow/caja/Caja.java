package com.styloflow.caja;

import com.styloflow.common.TenantScopedEntity;
import com.styloflow.usuarios.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Turno de caja: se abre con un fondo inicial y se cierra con el arqueo de efectivo. */
@Getter
@Setter
@Entity
@Table(name = "cajas")
public class Caja extends TenantScopedEntity {

    public enum Estado { ABIERTA, CERRADA }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abierta_por")
    private Usuario abiertaPor;

    @Column(name = "abierta_en", nullable = false)
    private Instant abiertaEn;

    @Column(name = "monto_inicial", nullable = false)
    private BigDecimal montoInicial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cerrada_por")
    private Usuario cerradaPor;

    @Column(name = "cerrada_en")
    private Instant cerradaEn;

    @Column(name = "efectivo_esperado")
    private BigDecimal efectivoEsperado;

    @Column(name = "efectivo_contado")
    private BigDecimal efectivoContado;

    private BigDecimal diferencia;

    @Column(name = "total_ventas")
    private BigDecimal totalVentas;

    private String observaciones;

    public boolean isAbierta() {
        return estado == Estado.ABIERTA;
    }
}
