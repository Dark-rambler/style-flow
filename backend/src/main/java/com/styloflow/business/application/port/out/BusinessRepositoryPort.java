package com.styloflow.business.application.port.out;

import com.styloflow.business.domain.model.BusinessModel;

import java.util.Optional;

public interface BusinessRepositoryPort {

    Optional<BusinessModel> findById(Long id);

    Optional<BusinessModel> findByCode(String code);

    BusinessModel save(BusinessModel business);
}
