package com.styloflow.auth.application.port.out;

import com.styloflow.auth.domain.model.AuthToken;
import com.styloflow.business.domain.model.BusinessModel;
import com.styloflow.platform.domain.model.SuperadminModel;
import com.styloflow.users.domain.model.UserModel;

public interface TokenPort {

    AuthToken generate(UserModel user, BusinessModel business);

    AuthToken generatePlatform(SuperadminModel superadmin);
}
