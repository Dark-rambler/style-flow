package com.styloflow.cashregister.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.domain.model.CashRegisterStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashRegisterJpaRepository extends JpaRepository<CashRegisterEntity, Long> {

    @EntityGraph(attributePaths = {"openedBy", "closedBy"})
    Optional<CashRegisterEntity> findFirstByStatus(CashRegisterStatus status);

    @EntityGraph(attributePaths = {"openedBy", "closedBy"})
    Page<CashRegisterEntity> findAllByOrderByOpenedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"openedBy", "closedBy"})
    Optional<CashRegisterEntity> findWithUsersById(Long id);
}
