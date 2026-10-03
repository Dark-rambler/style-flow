package com.styloflow.users.infrastructure.adapter.out.persistence;

import com.styloflow.users.domain.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<UserEntity> findAllByOrderByNameAsc();

    List<UserEntity> findByRoleAndActiveTrueOrderByNameAsc(Role role);

    long countByRoleAndActiveTrue(Role role);
}
