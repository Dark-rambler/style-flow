package com.styloflow.platform.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuperadminJpaRepository extends JpaRepository<SuperadminEntity, Long> {

    Optional<SuperadminEntity> findByUsernameIgnoreCase(String username);
}
