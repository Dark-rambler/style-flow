package com.styloflow.business.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessJpaRepository extends JpaRepository<BusinessEntity, Long> {

    Optional<BusinessEntity> findByCodeIgnoreCase(String code);
}
