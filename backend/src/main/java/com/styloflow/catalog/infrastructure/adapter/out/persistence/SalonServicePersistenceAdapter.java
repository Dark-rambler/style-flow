package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.application.port.out.SalonServiceRepositoryPort;
import com.styloflow.catalog.domain.model.SalonService;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalonServicePersistenceAdapter implements SalonServiceRepositoryPort {

    private final SalonServiceJpaRepository serviceRepository;
    private final CategoryJpaRepository categoryRepository;
    private final CatalogPersistenceMapper catalogMapper;

    @Override
    public List<SalonService> findAllWithCategory() {
        return catalogMapper.toServiceList(serviceRepository.findAllWithCategory());
    }

    @Override
    public Optional<SalonService> findById(Long id) {
        return serviceRepository.findById(id).map(catalogMapper::toDomain);
    }

    @Override
    public long countByCategory(Long categoryId) {
        return serviceRepository.countByCategoryId(categoryId);
    }

    @Override
    public SalonService save(SalonService service) {
        SalonServiceEntity entity = service.getId() == null ? new SalonServiceEntity()
                : serviceRepository.findById(service.getId())
                        .orElseThrow(() -> new NotFoundException("Service", service.getId()));
        catalogMapper.updateEntity(service, entity);
        entity.setCategory(categoryRepository.getReferenceById(service.getCategory().getId()));
        return catalogMapper.toDomain(serviceRepository.save(entity));
    }
}
