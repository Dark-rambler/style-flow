package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, Long> {

    @EntityGraph(attributePaths = "category")
    List<ServiceEntity> findAllByActiveAndCategoryActiveIsTrueOrderByCategoryNameAscNameAsc(boolean active);

    @NullMarked
    @Override
    @EntityGraph(attributePaths = "category")
    Optional<ServiceEntity> findById(Long id);

    long countByCategoryId(Long categoryId);

}
