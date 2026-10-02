package com.styloflow.customers.infrastructure.adapter.out.persistence;

import com.styloflow.customers.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerPersistenceMapper {

    Customer toDomain(CustomerEntity entity);

    void updateEntity(Customer customer, @MappingTarget CustomerEntity entity);
}
