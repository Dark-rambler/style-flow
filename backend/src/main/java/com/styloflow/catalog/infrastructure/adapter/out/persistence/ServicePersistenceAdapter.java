package com.styloflow.catalog.infrastructure.adapter.out.persistence;

import com.styloflow.catalog.application.port.out.ServiceRepositoryPort;
import com.styloflow.catalog.domain.model.Service;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServicePersistenceAdapter implements ServiceRepositoryPort {

    private final ServiceJpaRepository serviceRepository;
    private final CategoryJpaRepository categoryRepository;
    private final CatalogPersistenceMapper catalogMapper;

    @Override
    public List<Service> findAllSorted(boolean activeOnly) {
        return catalogMapper.toServiceList(serviceRepository.findAllWithCategoryByActiveOrderByCategoryNameAscNameAsc(activeOnly));
    }

    @Override
    public Optional<Service> findById(Long id) {
        return serviceRepository.findById(id).map(catalogMapper::toDomain);
    }

    @Override
    public long countByCategory(Long categoryId) {
        return serviceRepository.countByCategoryId(categoryId);
    }

    @Override
    public Service save(Service service) {
        ServiceEntity entity = service.getId() == null ?
                new ServiceEntity() :
                serviceRepository.findById(service.getId())
                        .orElseThrow(() -> new NotFoundException("Service", service.getId()));
        catalogMapper.updateEntity(service, entity);
        entity.setCategory(categoryRepository.getReferenceById(service.getCategory().getId()));
        return catalogMapper.toDomain(serviceRepository.save(entity));
    }
}
