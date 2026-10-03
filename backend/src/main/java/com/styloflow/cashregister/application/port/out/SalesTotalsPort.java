package com.styloflow.cashregister.application.port.out;

import com.styloflow.cashregister.domain.model.PaymentTotal;
import java.util.List;

public interface SalesTotalsPort {

    List<PaymentTotal> totalsByPaymentMethod(Long cashRegisterId);
}
