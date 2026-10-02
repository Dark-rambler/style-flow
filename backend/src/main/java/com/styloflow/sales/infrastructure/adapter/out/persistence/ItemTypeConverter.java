package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.sales.domain.model.ItemType;
import com.styloflow.shared.infrastructure.persistence.DbEnumConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class ItemTypeConverter extends DbEnumConverter<ItemType> {

    public ItemTypeConverter() {
        super(Map.of(ItemType.SERVICE, "SERVICIO", ItemType.PRODUCT, "PRODUCTO"));
    }
}
