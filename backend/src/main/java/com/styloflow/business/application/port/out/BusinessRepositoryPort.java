package com.styloflow.business.application.port.out;

import com.styloflow.business.domain.model.Business;
import java.util.Optional;

public interface BusinessRepositoryPort {

    Optional<Business> findById(Long id);

    Optional<Business> findByCode(String code);

    Business save(Business business);
}
