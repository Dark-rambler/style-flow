package com.styloflow.clientes;

import com.styloflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente extends BaseEntity {

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    private String email;

    @Column(name = "ci_nit")
    private String ciNit;

    private String notas;
}
