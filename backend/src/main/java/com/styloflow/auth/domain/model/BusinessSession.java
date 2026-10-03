package com.styloflow.auth.domain.model;

import com.styloflow.business.domain.model.Business;
import com.styloflow.users.domain.model.User;

public record BusinessSession(
        AuthToken token,
        User user,
        Business business
) {}
