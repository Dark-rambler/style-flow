package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.domain.model.SalonService;
import java.util.List;

/** Management of the services offered by the salon. */
public interface SalonServiceUseCase {

    /**
     * @param activeOnly whether to return only services available for sale
     * @return the services with their category
     */
    List<SalonService> list(boolean activeOnly);

    /**
     * @param command service data
     * @return the created service
     * @throws com.styloflow.shared.domain.exception.NotFoundException if the category does not exist
     */
    SalonService create(SalonServiceCommand command);

    /**
     * @param id service id
     * @param command new data
     * @return the updated service
     * @throws com.styloflow.shared.domain.exception.NotFoundException if the service or category does not exist
     */
    SalonService update(Long id, SalonServiceCommand command);
}
