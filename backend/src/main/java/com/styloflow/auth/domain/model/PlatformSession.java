package com.styloflow.auth.domain.model;

import com.styloflow.platform.domain.model.SuperadminModel;

public record PlatformSession(
        AuthToken token,
        SuperadminModel superadmin
) {}
