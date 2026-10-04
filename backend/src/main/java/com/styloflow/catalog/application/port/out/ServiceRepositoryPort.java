package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.ServiceModel;

import java.util.List;
import java.util.Optional;

public interface ServiceRepositoryPort {

    List<ServiceModel> findAllSorted();

    Optional<ServiceModel> findById(Long id);

    long countByCategory(Long categoryId);

    ServiceModel save(ServiceModel service);
}
