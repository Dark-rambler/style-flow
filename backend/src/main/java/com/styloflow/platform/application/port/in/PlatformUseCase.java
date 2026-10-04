package com.styloflow.platform.application.port.in;

import com.styloflow.platform.application.port.in.command.CreateBusinessCommand;
import com.styloflow.platform.domain.model.BusinessSummary;
import java.util.List;

public interface PlatformUseCase {

    List<BusinessSummary> list();

    BusinessSummary create(CreateBusinessCommand command);

    void changeStatus(Long businessId, boolean active);
}
