package com.styloflow.business.application.port.in;

import java.math.BigDecimal;

/**
 * New settings of the current business.
 *
 * @param name display name
 * @param taxId tax identification number
 * @param address postal address
 * @param phone contact phone
 * @param currency ISO 4217 currency code
 * @param currencySymbol symbol shown next to amounts
 * @param taxRate tax percentage included in prices
 * @param receiptMessage footer printed on receipts
 */
public record UpdateBusinessCommand(String name, String taxId, String address, String phone, String currency,
        String currencySymbol, BigDecimal taxRate, String receiptMessage) {}
