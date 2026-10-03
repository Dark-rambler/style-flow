package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.application.port.in.command.ServiceCommand;
import com.styloflow.catalog.domain.model.Service;

import java.util.List;

public interface ServiceUseCase {

    List<Service> list(boolean activeOnly);

    Service create(ServiceCommand command);

    Service update(Long id, ServiceCommand command);
}
