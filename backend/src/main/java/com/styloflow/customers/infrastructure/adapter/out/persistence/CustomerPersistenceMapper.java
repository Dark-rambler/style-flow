package com.styloflow.customers.infrastructure.adapter.out.persistence;

import com.styloflow.customers.domain.model.CustomerModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerPersistenceMapper {

    CustomerModel toDomain(CustomerEntity entity);

    void updateEntity(CustomerModel customer, @MappingTarget CustomerEntity entity);
}
