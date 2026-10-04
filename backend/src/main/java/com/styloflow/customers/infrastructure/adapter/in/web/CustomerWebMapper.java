package com.styloflow.customers.infrastructure.adapter.in.web;

import com.styloflow.customers.application.port.in.command.CustomerCommand;
import com.styloflow.customers.domain.model.CustomerModel;
import com.styloflow.customers.infrastructure.adapter.in.web.dto.CustomerRequest;
import com.styloflow.customers.infrastructure.adapter.in.web.dto.CustomerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerWebMapper {

    CustomerCommand toCommand(CustomerRequest request);

    CustomerResponse toResponse(CustomerModel customer);
}
