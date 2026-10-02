package com.styloflow.platform.application.port.in;

import com.styloflow.platform.domain.model.BusinessSummary;
import java.util.List;

/** Business (tenant) administration by the superadmin. */
public interface PlatformUseCase {

    List<BusinessSummary> list();

    /** Atomic sign-up of a business with its administrator and, optionally, the base catalog. */
    BusinessSummary create(CreateBusinessCommand command);

    /**
     * Activates or suspends a business.
     *
     * @param businessId business id
     * @param active {@code false} to suspend it
     * @throws com.styloflow.shared.domain.exception.NotFoundException if it does not exist
     */
    void changeStatus(Long businessId, boolean active);
}
