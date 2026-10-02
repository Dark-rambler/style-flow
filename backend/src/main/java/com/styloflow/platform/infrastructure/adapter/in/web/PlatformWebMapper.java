package com.styloflow.platform.infrastructure.adapter.in.web;

import com.styloflow.platform.application.port.in.CreateBusinessCommand;
import com.styloflow.platform.domain.model.BusinessSummary;
import com.styloflow.platform.infrastructure.adapter.in.web.dto.BusinessSummaryResponse;
import com.styloflow.platform.infrastructure.adapter.in.web.dto.CreateBusinessRequest;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlatformWebMapper {

    CreateBusinessCommand toCommand(CreateBusinessRequest request);

    BusinessSummaryResponse toResponse(BusinessSummary summary);

    List<BusinessSummaryResponse> toResponseList(List<BusinessSummary> summaries);
}
