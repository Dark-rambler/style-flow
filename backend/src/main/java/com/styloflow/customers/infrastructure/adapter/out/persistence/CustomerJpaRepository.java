package com.styloflow.customers.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    @Query("""
            select c from CustomerEntity c
            where :q = '' or lower(c.name) like lower(concat('%', :q, '%'))
               or c.phone like concat('%', :q, '%')
               or lower(c.taxId) like lower(concat('%', :q, '%'))
            """)
    Page<CustomerEntity> search(String q, Pageable pageable);
}
