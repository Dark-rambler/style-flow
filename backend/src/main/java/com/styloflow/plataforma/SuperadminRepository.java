package com.styloflow.plataforma;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuperadminRepository extends JpaRepository<Superadmin, Long> {

    Optional<Superadmin> findByUsernameIgnoreCase(String username);
}
