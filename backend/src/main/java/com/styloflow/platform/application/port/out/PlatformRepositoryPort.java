package com.styloflow.platform.application.port.out;

import com.styloflow.platform.domain.model.BusinessSummary;
import com.styloflow.platform.domain.model.NewBusiness;
import java.util.List;

public interface PlatformRepositoryPort {

    List<BusinessSummary> listSummaries();

    long register(NewBusiness business);
}
