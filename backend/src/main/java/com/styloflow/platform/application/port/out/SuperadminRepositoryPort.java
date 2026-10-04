package com.styloflow.platform.application.port.out;

import com.styloflow.platform.domain.model.SuperadminModel;

import java.util.Optional;

public interface SuperadminRepositoryPort {

    Optional<SuperadminModel> findByUsername(String username);

    long count();

    SuperadminModel save(SuperadminModel superadmin);
}
