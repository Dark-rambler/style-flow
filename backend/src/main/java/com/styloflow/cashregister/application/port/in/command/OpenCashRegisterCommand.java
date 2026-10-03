package com.styloflow.cashregister.application.port.in.command;

import java.math.BigDecimal;

public record OpenCashRegisterCommand(
        BigDecimal openingAmount,
        String notes
) {}
