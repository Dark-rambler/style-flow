package com.styloflow.customers.application.port.in;

import com.styloflow.customers.application.port.in.command.CustomerCommand;
import com.styloflow.customers.domain.model.Customer;
import com.styloflow.shared.domain.model.PageResult;

public interface CustomerUseCase {

    PageResult<Customer> search(String q, int page, int size);

    Customer get(Long id);

    Customer create(CustomerCommand command);

    Customer update(Long id, CustomerCommand command);
}
