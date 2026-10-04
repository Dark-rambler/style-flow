package com.styloflow.cash.infrastructure.adapter.out.persistence;

import com.styloflow.cash.domain.enums.CashStatus;
import com.styloflow.shared.infrastructure.persistence.DbEnumConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class CashStatusConverter extends DbEnumConverter<CashStatus> {

    public CashStatusConverter() {
        super(Map.of(CashStatus.OPEN, "ABIERTA", CashStatus.CLOSED, "CERRADA"));
    }
}
