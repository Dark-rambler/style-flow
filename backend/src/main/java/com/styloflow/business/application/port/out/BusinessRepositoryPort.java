package com.styloflow.business.application.port.out;

import com.styloflow.business.domain.model.Business;
import java.util.Optional;

/** Persistence of businesses (tenants). Not tenant-filtered: callers decide which business to read. */
public interface BusinessRepositoryPort {

    /**
     * @param id business id
     * @return the business, or empty
     */
    Optional<Business> findById(Long id);

    /**
     * @param code lowercase business code
     * @return the business, or empty
     */
    Optional<Business> findByCode(String code);

    /**
     * @param business business to persist
     * @return the persisted business
     */
    Business save(Business business);
}
