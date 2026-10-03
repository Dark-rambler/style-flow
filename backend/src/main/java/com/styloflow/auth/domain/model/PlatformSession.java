package com.styloflow.auth.domain.model;

import com.styloflow.platform.domain.model.Superadmin;

public record PlatformSession(
        AuthToken token,
        Superadmin superadmin
) {}
