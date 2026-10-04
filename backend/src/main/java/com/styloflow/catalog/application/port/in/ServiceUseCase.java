package com.styloflow.catalog.application.port.in;

import com.styloflow.catalog.application.port.in.command.ServiceCommand;
import com.styloflow.catalog.domain.model.ServiceModel;

import java.util.List;

public interface ServiceUseCase {

    List<ServiceModel> list(boolean active);

    ServiceModel create(ServiceCommand command);

    ServiceModel update(Long id, ServiceCommand command);
}
