package com.styloflow.users.domain.model;

/** Role of a business user; drives authorization ({@code @PreAuthorize}). */
public enum Role {
    ADMIN,
    CASHIER,
    STYLIST
}
