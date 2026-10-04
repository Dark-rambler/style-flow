package com.styloflow.customers.infrastructure.adapter.out.persistence;

import com.styloflow.customers.application.port.out.CustomerRepositoryPort;
import com.styloflow.customers.domain.model.CustomerModel;
import com.styloflow.shared.domain.exception.NotFoundException;
import com.styloflow.shared.domain.model.PageResult;
import com.styloflow.shared.infrastructure.persistence.PageResults;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository customerRepository;
    private final CustomerPersistenceMapper customerMapper;

    @Override
    public PageResult<CustomerModel> search(String q, int page, int size) {
        return PageResults.of(customerRepository.search(q, PageRequest.of(page, size, Sort.by("name"))),
                customerMapper::toDomain);
    }

    @Override
    public Optional<CustomerModel> findById(Long id) {
        return customerRepository.findById(id).map(customerMapper::toDomain);
    }

    @Override
    public CustomerModel save(CustomerModel customer) {
        var entity = customer.getId() == null ? new CustomerEntity() :
                customerRepository.findById(customer.getId())
                        .orElseThrow(() -> new NotFoundException("Customer", customer.getId()));
        customerMapper.updateEntity(customer, entity);
        return customerMapper.toDomain(customerRepository.save(entity));
    }
}
