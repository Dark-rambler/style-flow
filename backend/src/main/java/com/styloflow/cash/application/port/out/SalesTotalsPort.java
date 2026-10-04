package com.styloflow.cash.application.port.out;

import com.styloflow.cash.domain.model.PaymentTotal;
import java.util.List;

public interface SalesTotalsPort {

    List<PaymentTotal> totalsByPaymentMethod(Long cashId);
}
