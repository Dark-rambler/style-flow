package com.styloflow.customers.application.port.in;

import com.styloflow.customers.domain.model.Customer;
import com.styloflow.shared.domain.model.PageResult;

/** Management of the business customers. */
public interface CustomerUseCase {

    /** Searches by name, phone or tax id; an empty {@code q} lists everyone. */
    PageResult<Customer> search(String q, int page, int size);

    /**
     * @param id customer id
     * @return the customer
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    Customer get(Long id);

    /**
     * @param command customer data
     * @return the created customer
     */
    Customer create(CustomerCommand command);

    /**
     * @param id customer id
     * @param command new data
     * @return the updated customer
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    Customer update(Long id, CustomerCommand command);
}
