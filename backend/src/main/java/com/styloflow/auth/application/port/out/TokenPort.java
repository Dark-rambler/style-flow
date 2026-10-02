package com.styloflow.auth.application.port.out;

import com.styloflow.auth.domain.model.AuthToken;
import com.styloflow.business.domain.model.Business;
import com.styloflow.platform.domain.model.Superadmin;
import com.styloflow.users.domain.model.User;

public interface TokenPort {

    /** Token of a user inside their business: it carries the tenant. */
    AuthToken generate(User user, Business business);

    /** Platform token: no business, it can only use /api/platform/**. */
    AuthToken generatePlatform(Superadmin superadmin);
}
