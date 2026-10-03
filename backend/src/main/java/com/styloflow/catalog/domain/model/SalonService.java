package com.styloflow.catalog.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A service the salon sells (haircut, color…). */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalonService {

    private Long id;
    private Category category;
    private String name;
    private String description;
    private int durationMinutes;
    private BigDecimal price;
    private String imageUrl;
    private String imagePublicId;
    @Builder.Default
    private boolean active = true;

    /** Can be sold: both the service and its category are active. */
    public boolean isAvailable() {
        return active && category.isActive();
    }
}
