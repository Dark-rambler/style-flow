package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.Service;

import java.util.List;
import java.util.Optional;

public interface ServiceRepositoryPort {

    List<Service> findAllSorted(boolean activeOnly);

    Optional<Service> findById(Long id);

    long countByCategory(Long categoryId);

    Service save(Service service);
}
