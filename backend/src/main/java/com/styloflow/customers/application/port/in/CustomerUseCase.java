package com.styloflow.customers.application.port.in;

import com.styloflow.customers.application.port.in.command.CustomerCommand;
import com.styloflow.customers.domain.model.CustomerModel;
import com.styloflow.shared.domain.model.PageResult;

public interface CustomerUseCase {

    PageResult<CustomerModel> search(String q, int page, int size);

    CustomerModel get(Long id);

    CustomerModel create(CustomerCommand command);

    CustomerModel update(Long id, CustomerCommand command);
}
