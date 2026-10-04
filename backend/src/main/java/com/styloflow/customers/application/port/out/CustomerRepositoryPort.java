package com.styloflow.customers.application.port.out;

import com.styloflow.customers.domain.model.Customer;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CustomerRepositoryPort {

    PageResult<Customer> search(String q, int page, int size);

    Optional<Customer> findById(Long id);

    Customer save(Customer customer);
}
