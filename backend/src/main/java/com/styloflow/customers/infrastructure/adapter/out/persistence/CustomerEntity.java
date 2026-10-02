package com.styloflow.customers.infrastructure.adapter.out.persistence;

import com.styloflow.shared.infrastructure.persistence.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class CustomerEntity extends TenantScopedEntity {

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "telefono")
    private String phone;

    private String email;

    @Column(name = "ci_nit")
    private String taxId;

    @Column(name = "notas")
    private String notes;
}
