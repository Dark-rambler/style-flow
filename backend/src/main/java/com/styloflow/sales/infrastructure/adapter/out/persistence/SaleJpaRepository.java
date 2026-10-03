package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.sales.domain.enums.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleJpaRepository extends JpaRepository<SaleEntity, Long> {

    interface PaymentTotalView {
        PaymentMethod getPaymentMethod();
        long getCount();
        BigDecimal getTotal();
    }

    @Query("select s.paymentMethod as paymentMethod, count(s) as count, sum(s.total) as total " +
            "from SaleEntity s " +
            "where s.cash.id = :cashRegisterId and s.status = :status " +
            "group by s.paymentMethod")
    List<PaymentTotalView> totalsByPaymentMethod(Long cashRegisterId, SaleStatus status);

    @EntityGraph(attributePaths = {"cashier", "customer", "cashRegister", "cashRegister.openedBy", "cashRegister.closedBy"})
    @Query("select s from SaleEntity s " +
            "where s.date >= :from and s.date < :to " +
            "and (:customerId is null or s.customer.id = :customerId) " +
            "order by s.date desc")
    Page<SaleEntity> search(Instant from, Instant to, Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"cashier", "customer", "voidedBy", "items", "items.stylist", "cashRegister", "cashRegister.openedBy", "cashRegister.closedBy"})
    @Query("select s from SaleEntity s where s.id = :id")
    Optional<SaleEntity> findDetail(Long id);
}
