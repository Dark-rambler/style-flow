package com.styloflow.platform.application.port.in;

/**
 * Data to register a new business with its first administrator.
 *
 * @param name business name
 * @param code unique login code (stored lowercase)
 * @param taxId optional tax id
 * @param phone optional phone
 * @param adminName name of the first administrator
 * @param adminUsername username of the first administrator
 * @param adminPassword plain-text password of the first administrator
 * @param baseCatalog whether to seed the base catalog
 */
public record CreateBusinessCommand(String name, String code, String taxId, String phone, String adminName,
        String adminUsername, String adminPassword, boolean baseCatalog) {}
