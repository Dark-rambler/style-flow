package com.styloflow.business.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessModel {

    private Long id;
    private String code;
    @Builder.Default
    private boolean active = true;
    private String name;
    private String taxId;
    private String address;
    private String phone;
    private String currency;
    private String currencySymbol;
    private BigDecimal taxRate;
    private String receiptMessage;
    private Instant createdAt;
}
