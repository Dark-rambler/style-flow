package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
