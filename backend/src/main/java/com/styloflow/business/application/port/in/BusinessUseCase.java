package com.styloflow.business.application.port.in;

import com.styloflow.business.domain.model.Business;

/** Settings of the business of the current session. */
public interface BusinessUseCase {

    Business getCurrent();

    /**
     * Updates the settings of the current business.
     *
     * @param command new settings
     * @return the updated business
     */
    Business update(UpdateBusinessCommand command);
}
