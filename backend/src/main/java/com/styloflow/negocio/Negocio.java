package com.styloflow.negocio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Un negocio (tenant) de la plataforma y su configuración: datos del ticket, moneda e IVA. */
@Getter
@Setter
@Entity
@Table(name = "negocio")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador corto que se escribe al iniciar sesión (p. ej. "salon-bella"). No se puede cambiar. */
    @Column(nullable = false, unique = true, updatable = false)
    private String codigo;

    @Column(nullable = false)
    private boolean activo = true;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
