package com.styloflow.negocio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

/** Configuración del negocio: fila única (id = 1). */
@Getter
@Setter
@Entity
@Table(name = "negocio")
public class Negocio {

    public static final short ID = 1;

    @Id
    private Short id = ID;

    @Column(nullable = false)
    private String nombre;

    private String nit;

    private String direccion;

    private String telefono;

    @Column(nullable = false)
    private String moneda;

    @Column(nullable = false)
    private String simbolo;

    @Column(name = "iva_porcentaje", nullable = false)
    private BigDecimal ivaPorcentaje;

    @Column(name = "mensaje_ticket")
    private String mensajeTicket;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
