package com.styloflow.sales.domain.model;

/** How a sale was paid. Only {@link #CASH} computes change. */
public enum PaymentMethod {
    CASH,
    QR,
    CARD,
    TRANSFER
}
