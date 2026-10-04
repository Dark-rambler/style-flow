package com.styloflow.customers.application.service;

import com.styloflow.customers.application.port.in.command.CustomerCommand;
import com.styloflow.customers.application.port.in.CustomerUseCase;
import com.styloflow.customers.application.port.out.CustomerRepositoryPort;
import com.styloflow.customers.domain.model.CustomerModel;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.shared.domain.model.TextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements CustomerUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    public PageResult<CustomerModel> search(String q, int page, int size) {
        return customerRepository.search(q == null ? "" : q.trim(), page, Math.min(size, 100));
    }

    @Override
    public CustomerModel get(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }

    @Override
    @Transactional
    public CustomerModel create(CustomerCommand command) {
        var customer = new CustomerModel();
        apply(customer, command);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public CustomerModel update(Long id, CustomerCommand command) {
        var customer = get(id);
        apply(customer, command);
        return customerRepository.save(customer);
    }

    private static void apply(CustomerModel customer, CustomerCommand command) {
        customer.setName(command.name().trim());
        customer.setPhone(TextUtils.blankToNull(command.phone()));
        customer.setEmail(TextUtils.blankToNull(command.email()));
        customer.setTaxId(TextUtils.blankToNull(command.taxId()));
        customer.setNotes(TextUtils.blankToNull(command.notes()));
    }
}
