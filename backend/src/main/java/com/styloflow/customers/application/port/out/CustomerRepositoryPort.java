package com.styloflow.customers.application.port.out;

import com.styloflow.customers.domain.model.CustomerModel;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CustomerRepositoryPort {

    PageResult<CustomerModel> search(String q, int page, int size);

    Optional<CustomerModel> findById(Long id);

    CustomerModel save(CustomerModel customer);
}
