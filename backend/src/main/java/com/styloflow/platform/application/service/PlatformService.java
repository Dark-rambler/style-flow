package com.styloflow.platform.application.service;

import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.business.domain.model.Business;
import com.styloflow.platform.application.port.in.BusinessStatusUseCase;
import com.styloflow.platform.application.port.in.command.CreateBusinessCommand;
import com.styloflow.platform.application.port.in.PlatformUseCase;
import com.styloflow.platform.application.port.out.PlatformRepositoryPort;
import com.styloflow.platform.domain.model.BaseCatalog;
import com.styloflow.platform.domain.model.BusinessSummary;
import com.styloflow.platform.domain.model.NewBusiness;
import com.styloflow.shared.application.port.out.PasswordHasherPort;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.TextUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlatformService implements PlatformUseCase {

    private final PlatformRepositoryPort platformRepository;
    private final BusinessRepositoryPort businessRepository;
    private final BusinessStatusUseCase businessStatus;
    private final PasswordHasherPort passwordHasher;

    @Override
    @Transactional(readOnly = true)
    public List<BusinessSummary> list() {
        return platformRepository.listSummaries();
    }

    @Override
    @Transactional
    public BusinessSummary create(CreateBusinessCommand command) {
        String code = command.code().trim().toLowerCase();
        if (businessRepository.findByCode(code).isPresent())
            throw new BusinessRuleException("A business with code '" + code + "' already exists");
        long id = platformRepository.register(new NewBusiness(code, command.name().trim(),
                TextUtils.blankToNull(command.taxId()), TextUtils.blankToNull(command.phone()),
                command.adminName().trim(), command.adminUsername().trim().toLowerCase(),
                passwordHasher.hash(command.adminPassword()),
                command.baseCatalog() ? BaseCatalog.CATEGORIES : List.of()));
        return platformRepository.listSummaries()
                .stream()
                .filter(b -> b.id() == id)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public void changeStatus(Long businessId, boolean active) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business", businessId));
        business.setActive(active);
        businessRepository.save(business);
        businessStatus.evict(businessId);
    }
}
