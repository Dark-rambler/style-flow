package com.styloflow.cashregister.application.port.in;

import java.math.BigDecimal;

/**
 * Cash count that closes the open cash register.
 *
 * @param countedCash cash physically counted
 * @param notes optional closing notes
 */
public record CloseCashRegisterCommand(BigDecimal countedCash, String notes) {}
