package com.styloflow.auth.application.port.in;

import com.styloflow.auth.domain.model.BusinessSession;
import com.styloflow.auth.domain.model.PlatformSession;
import com.styloflow.users.domain.model.User;

public interface AuthUseCase {

    /** Login of a user inside their business. */
    BusinessSession login(LoginCommand command);

    /** The authenticated user, if still active. */
    User currentUser(Long userId);

    /** Login of the platform superadmin (token without business). */
    PlatformSession platformLogin(PlatformLoginCommand command);
}
