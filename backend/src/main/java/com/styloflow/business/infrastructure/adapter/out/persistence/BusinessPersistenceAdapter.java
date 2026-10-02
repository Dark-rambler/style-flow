package com.styloflow.business.infrastructure.adapter.out.persistence;

import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.business.domain.model.Business;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** New businesses are inserted with SQL by {@code PlatformJdbcAdapter}; here they are only read and updated. */
@Component
@RequiredArgsConstructor
public class BusinessPersistenceAdapter implements BusinessRepositoryPort {

    private final BusinessJpaRepository businessRepository;
    private final BusinessPersistenceMapper businessMapper;

    @Override
    public Optional<Business> findById(Long id) {
        return businessRepository.findById(id).map(businessMapper::toDomain);
    }

    @Override
    public Optional<Business> findByCode(String code) {
        return businessRepository.findByCodeIgnoreCase(code).map(businessMapper::toDomain);
    }

    @Override
    public Business save(Business business) {
        BusinessEntity entity = businessRepository.findById(business.getId())
                .orElseThrow(() -> new NotFoundException("Business", business.getId()));
        businessMapper.updateEntity(business, entity);
        return businessMapper.toDomain(businessRepository.save(entity));
    }
}
