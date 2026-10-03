package com.styloflow.business.application.port.in.command;

import java.math.BigDecimal;

public record UpdateBusinessCommand(
        String name,
        String taxId,
        String address,
        String phone,
        String currency,
        String currencySymbol,
        BigDecimal taxRate,
        String receiptMessage
) {}
