package com.styloflow.sales.infrastructure.adapter.out.persistence;

import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.shared.infrastructure.persistence.DbEnumConverter;
import jakarta.persistence.Converter;
import java.util.Map;

@Converter(autoApply = true)
public class PaymentMethodConverter extends DbEnumConverter<PaymentMethod> {

    public PaymentMethodConverter() {
        super(Map.of(
                PaymentMethod.CASH, "EFECTIVO",
                PaymentMethod.QR, "QR",
                PaymentMethod.CARD, "TARJETA",
                PaymentMethod.TRANSFER, "TRANSFERENCIA")
        );
    }
}
