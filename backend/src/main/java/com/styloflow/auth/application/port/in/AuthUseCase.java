package com.styloflow.auth.application.port.in;

import com.styloflow.auth.application.port.in.command.LoginCommand;
import com.styloflow.auth.application.port.in.command.PlatformLoginCommand;
import com.styloflow.auth.domain.model.BusinessSession;
import com.styloflow.auth.domain.model.PlatformSession;
import com.styloflow.users.domain.model.User;

public interface AuthUseCase {

    BusinessSession login(LoginCommand command);

    User currentUser(Long userId);

    PlatformSession platformLogin(PlatformLoginCommand command);
}
