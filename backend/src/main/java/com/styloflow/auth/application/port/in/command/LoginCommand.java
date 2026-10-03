package com.styloflow.auth.application.port.in.command;

public record LoginCommand(
        String businessCode,
        String username,
        String password
) {}
