package com.styloflow.auth.application.port.in.command;

public record PlatformLoginCommand(
        String username,
        String password
) {}
