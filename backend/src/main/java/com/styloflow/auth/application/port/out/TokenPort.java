package com.styloflow.auth.application.port.out;

import com.styloflow.auth.domain.model.AuthToken;
import com.styloflow.business.domain.model.Business;
import com.styloflow.platform.domain.model.Superadmin;
import com.styloflow.users.domain.model.User;

public interface TokenPort {

    AuthToken generate(User user, Business business);

    AuthToken generatePlatform(Superadmin superadmin);
}
