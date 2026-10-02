package com.styloflow.auth.application.port.in;

/**
 * Credentials for a platform superadmin login.
 *
 * @param username login name
 * @param password plain-text password
 */
public record PlatformLoginCommand(String username, String password) {}
