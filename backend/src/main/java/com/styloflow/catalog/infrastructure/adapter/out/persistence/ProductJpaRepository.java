package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByActiveOrderByNameAsc(boolean active);

    boolean existsBySkuIgnoreCase(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.id = :id")
    Optional<ProductEntity> findByIdUpdate(Long id);

    @Query("select p from ProductEntity p where p.active = true and p.stock <= p.minStock order by p.stock")
    List<ProductEntity> findLowStock();
}
