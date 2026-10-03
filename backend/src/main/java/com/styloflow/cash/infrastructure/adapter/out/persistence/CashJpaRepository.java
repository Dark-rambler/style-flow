package com.styloflow.cash.infrastructure.adapter.out.persistence;

import com.styloflow.cash.domain.model.CashStatus;
import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashJpaRepository extends JpaRepository<CashEntity, Long> {

    @EntityGraph(attributePaths = {"openedBy", "closedBy"})
    Optional<CashEntity> findFirstByStatus(CashStatus status);

    @EntityGraph(attributePaths = {"openedBy", "closedBy"})
    Page<CashEntity> findAllByOrderByOpenedAtDesc(Pageable pageable);

    @NullMarked
    @EntityGraph(attributePaths = {"openedBy", "closedBy"})
    Optional<CashEntity> findById(Long id);
}
