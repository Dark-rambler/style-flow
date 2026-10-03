package com.styloflow.catalog.application.service;

import com.styloflow.catalog.application.port.in.command.ServiceCommand;
import com.styloflow.catalog.application.port.in.ServiceUseCase;
import com.styloflow.catalog.application.port.out.CategoryRepositoryPort;
import com.styloflow.catalog.application.port.out.ServiceRepositoryPort;
import com.styloflow.catalog.application.utils.ImageFilesUtil;
import com.styloflow.catalog.domain.model.Service;
import com.styloflow.shared.application.port.out.CurrentTenantPort;
import com.styloflow.shared.application.port.out.ImageStoragePort;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceService implements ServiceUseCase {

    private final ServiceRepositoryPort serviceRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final ImageStoragePort imageStorage;
    private final CurrentTenantPort currentTenant;

    @Override
    public List<Service> list(boolean activeOnly) {
        return serviceRepository.findAllSorted(activeOnly);
    }

    @Override
    @Transactional
    public Service create(ServiceCommand command) {
        var service = new Service();
        apply(service, command);
        if (command.image() == null)
            return serviceRepository.save(service);
        var image = imageStorage.upload(ImageFilesUtil.bytes(command.image()), "business-" + currentTenant.businessId() + "/services");
        service.setImageUrl(image.url());
        service.setImagePublicId(image.publicId());
        try {
            return serviceRepository.save(service);
        } catch (RuntimeException e) {
            imageStorage.delete(image.publicId());
            throw e;
        }
    }

    @Override
    @Transactional
    public Service update(Long id, ServiceCommand command) {
        var service = serviceRepository.findById(id).orElseThrow(() -> new NotFoundException("Service", id));
        apply(service, command);
        return serviceRepository.save(service);
    }

    private void apply(Service service, ServiceCommand command) {
        service.setCategory(categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", command.categoryId())));
        service.setName(command.name().trim());
        service.setDescription(command.description());
        service.setDurationMinutes(command.durationMinutes());
        service.setPrice(command.price());
        if (command.active() != null)
            service.setActive(command.active());
    }
}
