package com.styloflow.business.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A business (tenant) of the platform and its settings: receipt data, currency and tax. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business {

    private Long id;
    /** Short identifier typed at login (e.g. "salon-bella"). It cannot be changed. */
    private String code;
    @Builder.Default
    private boolean active = true;
    private String name;
    private String taxId;
    private String address;
    private String phone;
    private String currency;
    private String currencySymbol;
    /** VAT included in prices, as a percentage. */
    private BigDecimal taxRate;
    private String receiptMessage;
    private Instant createdAt;
}
