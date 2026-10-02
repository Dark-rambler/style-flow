package com.styloflow.catalog.application.service;

import com.styloflow.catalog.application.port.in.SalonServiceCommand;
import com.styloflow.catalog.application.port.in.SalonServiceUseCase;
import com.styloflow.catalog.application.port.out.CategoryRepositoryPort;
import com.styloflow.catalog.application.port.out.SalonServiceRepositoryPort;
import com.styloflow.catalog.domain.model.SalonService;
import com.styloflow.shared.domain.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default {@link SalonServiceUseCase} implementation. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalonServiceService implements SalonServiceUseCase {

    private final SalonServiceRepositoryPort serviceRepository;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    public List<SalonService> list(boolean activeOnly) {
        return serviceRepository.findAllWithCategory().stream()
                .filter(s -> !activeOnly || s.isAvailable())
                .toList();
    }

    @Override
    @Transactional
    public SalonService create(SalonServiceCommand command) {
        SalonService service = new SalonService();
        apply(service, command);
        return serviceRepository.save(service);
    }

    @Override
    @Transactional
    public SalonService update(Long id, SalonServiceCommand command) {
        SalonService service = serviceRepository.findById(id).orElseThrow(() -> new NotFoundException("Service", id));
        apply(service, command);
        return serviceRepository.save(service);
    }

    private void apply(SalonService service, SalonServiceCommand command) {
        service.setCategory(categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", command.categoryId())));
        service.setName(command.name().trim());
        service.setDescription(command.description());
        service.setDurationMinutes(command.durationMinutes());
        service.setPrice(command.price());
        if (command.active() != null) {
            service.setActive(command.active());
        }
    }
}
