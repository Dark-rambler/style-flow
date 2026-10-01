package com.styloflow.usuarios;

import com.styloflow.common.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario extends TenantScopedEntity {

    @Column(nullable = false)
    private String nombre;

    /** Único dentro del negocio (índice uq_usuarios_negocio_username). */
    @Column(nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    private String telefono;

    @Column(name = "comision_porcentaje", nullable = false)
    private BigDecimal comisionPorcentaje = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;
}
