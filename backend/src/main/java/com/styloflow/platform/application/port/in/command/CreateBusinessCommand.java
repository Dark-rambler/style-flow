package com.styloflow.platform.application.port.in.command;

public record CreateBusinessCommand(
        String name,
        String code,
        String taxId,
        String phone,
        String adminName,
        String adminUsername,
        String adminPassword,
        boolean baseCatalog
) {}
