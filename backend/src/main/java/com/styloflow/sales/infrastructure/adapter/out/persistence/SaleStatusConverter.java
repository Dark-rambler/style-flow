package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.sales.domain.enums.SaleStatus;
import com.styloflow.shared.infrastructure.persistence.DbEnumConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class SaleStatusConverter extends DbEnumConverter<SaleStatus> {

    public SaleStatusConverter() {
        super(Map.of(SaleStatus.COMPLETED, "COMPLETADA", SaleStatus.VOIDED, "ANULADA"));
    }
}
