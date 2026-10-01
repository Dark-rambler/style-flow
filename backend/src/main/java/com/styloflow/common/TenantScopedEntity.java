package com.styloflow.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

/**
 * Entidad que pertenece a un negocio. Hibernate completa {@code negocioId} con el tenant actual al insertar y
 * filtra por él todas las consultas JPQL y cargas por id. El SQL nativo debe filtrar {@code negocio_id} a mano.
 */
@Getter
@MappedSuperclass
public abstract class TenantScopedEntity extends BaseEntity {

    @TenantId
    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long negocioId;
}
