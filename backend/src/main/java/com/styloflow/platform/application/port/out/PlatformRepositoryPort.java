package com.styloflow.platform.application.port.out;

import com.styloflow.platform.domain.model.BusinessSummary;
import com.styloflow.platform.domain.model.NewBusiness;
import java.util.List;

public interface PlatformRepositoryPort {

    /** @return every business with its usage counters */
    List<BusinessSummary> listSummaries();

    /** Inserts the business, its administrator and the catalog; returns the business id. */
    long register(NewBusiness business);
}
