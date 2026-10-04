package com.styloflow.business.application.port.in;

import com.styloflow.business.application.port.in.command.UpdateBusinessCommand;
import com.styloflow.business.domain.model.BusinessModel;

public interface BusinessUseCase {

    BusinessModel getCurrent();

    BusinessModel update(UpdateBusinessCommand command);
}
