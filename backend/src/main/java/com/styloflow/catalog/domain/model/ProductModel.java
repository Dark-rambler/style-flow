package com.styloflow.catalog.domain.model;

import com.styloflow.shared.domain.exception.BusinessRuleException;
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
public class ProductModel {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String imageUrl;
    private String imagePublicId;
    private int stock;
    private int minStock;
    @Builder.Default
    private boolean active = true;

    public void decreaseStock(int quantity) {
        if (quantity > stock)
            throw new BusinessRuleException("Insufficient stock for '" + name + "' (available: " + stock + ")");
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        stock += quantity;
    }

    public void adjustStock(int quantity) {
        if (quantity < 0)
            decreaseStock(-quantity);
        else
            increaseStock(quantity);
    }

    public boolean isLowStock() {
        return stock <= minStock;
    }
}
