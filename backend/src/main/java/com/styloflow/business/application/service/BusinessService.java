package com.styloflow.business.application.service;

import com.styloflow.business.application.port.in.BusinessUseCase;
import com.styloflow.business.application.port.in.command.UpdateBusinessCommand;
import com.styloflow.business.application.port.out.BusinessRepositoryPort;
import com.styloflow.business.domain.model.Business;
import com.styloflow.shared.application.port.out.CurrentTenantPort;
import com.styloflow.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessService implements BusinessUseCase {

    private final BusinessRepositoryPort businessRepository;
    private final CurrentTenantPort currentTenant;

    @Override
    public Business getCurrent() {
        long id = currentTenant.businessId();
        return businessRepository.findById(id).orElseThrow(() -> new NotFoundException("Business", id));
    }

    @Override
    @Transactional
    public Business update(UpdateBusinessCommand command) {
        Business business = getCurrent();
        apply(business, command);
        return businessRepository.save(business);
    }

    private void apply(Business business, UpdateBusinessCommand command) {
        business.setName(command.name().trim());
        business.setTaxId(command.taxId());
        business.setAddress(command.address());
        business.setPhone(command.phone());
        business.setCurrency(command.currency().toUpperCase());
        business.setCurrencySymbol(command.currencySymbol());
        business.setTaxRate(command.taxRate());
        business.setReceiptMessage(command.receiptMessage());
    }
}
