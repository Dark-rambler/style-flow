package com.styloflow.sales.infrastructure.adapter.in.web.dto;

import com.styloflow.sales.domain.model.ItemType;
import java.math.BigDecimal;

public record SaleItemResponse(Long id, ItemType type, Long itemId, String description, int quantity,
        BigDecimal unitPrice, BigDecimal discount, BigDecimal subtotal, Long stylistId, String stylist) {}
