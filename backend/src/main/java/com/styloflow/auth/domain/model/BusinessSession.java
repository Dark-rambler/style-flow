package com.styloflow.auth.domain.model;

import com.styloflow.business.domain.model.BusinessModel;
import com.styloflow.users.domain.model.UserModel;

public record BusinessSession(
        AuthToken token,
        UserModel user,
        BusinessModel business
) {}
