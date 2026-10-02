package com.styloflow.cashregister.infrastructure.adapter.out.persistence;

import com.styloflow.cashregister.domain.model.CashRegisterStatus;
import com.styloflow.shared.infrastructure.persistence.DbEnumConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class CashRegisterStatusConverter extends DbEnumConverter<CashRegisterStatus> {

    public CashRegisterStatusConverter() {
        super(Map.of(CashRegisterStatus.OPEN, "ABIERTA", CashRegisterStatus.CLOSED, "CERRADA"));
    }
}
