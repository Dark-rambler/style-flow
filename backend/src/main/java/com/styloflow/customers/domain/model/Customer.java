package com.styloflow.customers.domain.model;

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
public class Customer {

    private Long id;
    private String name;
    private String phone;
    private String email;
    /** CI or NIT (identity or tax number), printed on the receipt. */
    private String taxId;
    private String notes;
    private Instant createdAt;
}
