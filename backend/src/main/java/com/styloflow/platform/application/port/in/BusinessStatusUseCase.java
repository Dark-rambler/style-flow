package com.styloflow.platform.application.port.in;

/** Active/suspended status of each business, checked on every request. */
public interface BusinessStatusUseCase {

    boolean isActive(Long businessId);

    /**
     * Drops the cached status so the next request reads it again.
     *
     * @param businessId business id
     */
    void evict(Long businessId);
}
