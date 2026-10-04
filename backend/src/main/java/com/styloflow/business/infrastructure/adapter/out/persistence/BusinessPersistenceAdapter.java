package com.styloflow.business.infrastructure.adapter.out.persistence;

import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.business.domain.model.BusinessModel;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessPersistenceAdapter implements BusinessRepositoryPort {

    private final BusinessJpaRepository businessRepository;
    private final BusinessPersistenceMapper businessMapper;

    @Override
    public Optional<BusinessModel> findById(Long id) {
        return businessRepository.findById(id).map(businessMapper::toDomain);
    }

    @Override
    public Optional<BusinessModel> findByCode(String code) {
        return businessRepository.findByCodeIgnoreCase(code).map(businessMapper::toDomain);
    }

    @Override
    public BusinessModel save(BusinessModel business) {
        var entity = businessRepository.findById(business.getId())
                .orElseThrow(() -> new NotFoundException("Business", business.getId()));
        businessMapper.updateEntity(business, entity);
        return businessMapper.toDomain(businessRepository.save(entity));
    }
}
