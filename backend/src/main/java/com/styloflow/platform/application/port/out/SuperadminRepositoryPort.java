package com.styloflow.platform.application.port.out;

import com.styloflow.platform.domain.model.Superadmin;
import java.util.Optional;

public interface SuperadminRepositoryPort {

    Optional<Superadmin> findByUsername(String username);

    long count();

    Superadmin save(Superadmin superadmin);
}
