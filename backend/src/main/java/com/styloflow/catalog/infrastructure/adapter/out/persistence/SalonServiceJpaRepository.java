package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalonServiceJpaRepository extends JpaRepository<SalonServiceEntity, Long> {

    @EntityGraph(attributePaths = "category")
    @Query("select s from SalonServiceEntity s order by s.category.name, s.name")
    List<SalonServiceEntity> findAllWithCategory();

    @NullMarked
    @Override
    @EntityGraph(attributePaths = "category")
    Optional<SalonServiceEntity> findById(Long id);

    long countByCategoryId(Long categoryId);
}
