package com.styloflow.customers.application.port.out;

import com.styloflow.customers.domain.model.Customer;
import com.styloflow.shared.domain.model.PageResult;
import java.util.Optional;

public interface CustomerRepositoryPort {

    /** Sorted by name. */
    PageResult<Customer> search(String q, int page, int size);

    /**
     * @param id customer id
     * @return the customer, or empty
     */
    Optional<Customer> findById(Long id);

    /**
     * @param customer customer to persist
     * @return the persisted customer
     */
    Customer save(Customer customer);
}
