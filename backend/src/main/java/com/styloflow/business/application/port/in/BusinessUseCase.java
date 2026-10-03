package com.styloflow.business.application.port.in;

import com.styloflow.business.application.port.in.command.UpdateBusinessCommand;
import com.styloflow.business.domain.model.Business;

public interface BusinessUseCase {

    Business getCurrent();

    Business update(UpdateBusinessCommand command);
}
