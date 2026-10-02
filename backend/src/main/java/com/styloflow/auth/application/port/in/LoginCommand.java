package com.styloflow.auth.application.port.in;

/**
 * Credentials for a business user login.
 *
 * @param businessCode code of the business (tenant) the user belongs to
 * @param username login name
 * @param password plain-text password
 */
public record LoginCommand(String businessCode, String username, String password) {}
