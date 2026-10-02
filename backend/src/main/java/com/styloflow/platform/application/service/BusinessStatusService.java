package com.styloflow.platform.application.service;

import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.business.domain.model.Business;
import com.styloflow.platform.application.port.in.BusinessStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cached: {@code ActiveBusinessFilter} checks it on every request. */
@Service
@RequiredArgsConstructor
public class BusinessStatusService implements BusinessStatusUseCase {

    static final String CACHE = "activeBusiness";

    private final BusinessRepositoryPort businessRepository;

    @Override
    @Cacheable(CACHE)
    @Transactional(readOnly = true)
    public boolean isActive(Long businessId) {
        return businessRepository.findById(businessId).map(Business::isActive).orElse(false);
    }

    @Override
    @CacheEvict(cacheNames = CACHE, key = "#businessId")
    public void evict(Long businessId) {
        // only evicts the cache entry
    }
}
