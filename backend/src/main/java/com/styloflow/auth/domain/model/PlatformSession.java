package com.styloflow.auth.domain.model;

import com.styloflow.platform.domain.model.Superadmin;

/**
 * Result of a successful platform login.
 *
 * @param token access token without business claims
 * @param superadmin authenticated superadmin
 */
public record PlatformSession(AuthToken token, Superadmin superadmin) {}
