package com.styloflow.catalog.application.port.out;

import com.styloflow.catalog.domain.model.SalonService;
import java.util.List;
import java.util.Optional;

public interface SalonServiceRepositoryPort {

    /** Sorted by category and name. */
    List<SalonService> findAllWithCategory();

    /**
     * @param id service id
     * @return the service, or empty
     */
    Optional<SalonService> findById(Long id);

    /**
     * @param categoryId category id
     * @return number of services in the category
     */
    long countByCategory(Long categoryId);

    /**
     * @param service service to persist
     * @return the persisted service
     */
    SalonService save(SalonService service);
}
