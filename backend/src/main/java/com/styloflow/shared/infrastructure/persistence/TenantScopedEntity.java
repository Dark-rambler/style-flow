package com.styloflow.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.TenantId;

/**
 * Entity owned by a business. Hibernate fills {@code businessId} with the current tenant on insert and filters
 * every JPQL query and load by id with it. Native SQL must filter {@code negocio_id} explicitly.
 */
@Getter
@MappedSuperclass
public abstract class TenantScopedEntity extends BaseEntity {

    @TenantId
    @Column(name = "negocio_id", nullable = false, updatable = false)
    private Long businessId;
}
