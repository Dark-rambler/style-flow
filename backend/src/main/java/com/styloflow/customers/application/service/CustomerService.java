package com.styloflow.customers.application.service;

import com.styloflow.customers.application.port.in.command.CustomerCommand;
import com.styloflow.customers.application.port.in.CustomerUseCase;
import com.styloflow.customers.application.port.out.CustomerRepositoryPort;
import com.styloflow.customers.domain.model.Customer;
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
    public PageResult<Customer> search(String q, int page, int size) {
        return customerRepository.search(q == null ? "" : q.trim(), page, Math.min(size, 100));
    }

    @Override
    public Customer get(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }

    @Override
    @Transactional
    public Customer create(CustomerCommand command) {
        Customer customer = new Customer();
        apply(customer, command);
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Customer update(Long id, CustomerCommand command) {
        Customer customer = get(id);
        apply(customer, command);
        return customerRepository.save(customer);
    }

    private static void apply(Customer customer, CustomerCommand command) {
        customer.setName(command.name().trim());
        customer.setPhone(TextUtils.blankToNull(command.phone()));
        customer.setEmail(TextUtils.blankToNull(command.email()));
        customer.setTaxId(TextUtils.blankToNull(command.taxId()));
        customer.setNotes(TextUtils.blankToNull(command.notes()));
    }
}
