package com.styloflow.catalog.domain.model;

import java.math.BigDecimal;
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
public class ServiceModel {

    private Long id;
    private CategoryModel category;
    private String name;
    private String description;
    private int durationMinutes;
    private BigDecimal price;
    private String imageUrl;
    private String imagePublicId;
    @Builder.Default
    private boolean active = true;

    public boolean isAvailable() {
        return active && category.isActive();
    }
}
