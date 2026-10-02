package com.styloflow.auth.domain.model;

import com.styloflow.business.domain.model.Business;
import com.styloflow.users.domain.model.User;

/**
 * Result of a successful business login.
 *
 * @param token access token carrying the {@code bid} claim
 * @param user authenticated user
 * @param business business (tenant) of the user
 */
public record BusinessSession(AuthToken token, User user, Business business) {}
