package com.styloflow.business.infrastructure.adapter.in.web;

import com.styloflow.business.application.port.in.UpdateBusinessCommand;
import com.styloflow.business.domain.model.Business;
import com.styloflow.business.infrastructure.adapter.in.web.dto.BusinessRequest;
import com.styloflow.business.infrastructure.adapter.in.web.dto.BusinessResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BusinessWebMapper {

    UpdateBusinessCommand toCommand(BusinessRequest request);

    BusinessResponse toResponse(Business business);
}
