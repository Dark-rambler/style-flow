package com.styloflow.cash.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.styloflow.sales.domain.enums.PaymentMethod;
import com.styloflow.shared.domain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Cash register cash count")
class CashTest {

    private static final Instant NOW = Instant.parse("2026-10-02T22:00:00Z");
    private static final List<PaymentTotal> SALES = List.of(
            new PaymentTotal(PaymentMethod.CASH, 3, new BigDecimal("150.00")),
            new PaymentTotal(PaymentMethod.QR, 2, new BigDecimal("80.00")));

    private final Cash register = Cash.open(null, NOW, new BigDecimal("100.00"), "morning");

    @Test
    void expectedCash_should_beFloatPlusCashSales_when_open() {
        assertThat(register.expectedCash(SALES)).isEqualTo("250.00");
    }

    @Test
    void close_should_recordCountAndDifference() {
        register.close(null, NOW, new BigDecimal("245.00"), "short 5", SALES);

        assertAll(
                () -> assertThat(register.isOpen()).isFalse(),
                () -> assertThat(register.getExpectedCash()).isEqualTo("250.00"),
                () -> assertThat(register.getSalesTotal()).isEqualTo("230.00"),
                () -> assertThat(register.getDifference()).isEqualTo("-5.00"),
                () -> assertThat(register.getNotes()).isEqualTo("short 5"),
                () -> assertThat(register.expectedCash(List.of())).isEqualTo("250.00"));
    }

    @Test
    void close_should_keepOpeningNotes_when_closingNotesBlank() {
        register.close(null, NOW, new BigDecimal("250.00"), " ", SALES);

        assertThat(register.getNotes()).isEqualTo("morning");
    }

    @Test
    void close_should_throw_when_alreadyClosed() {
        register.close(null, NOW, new BigDecimal("250.00"), null, SALES);

        assertThatThrownBy(() -> register.close(null, NOW, BigDecimal.ZERO, null, SALES))
                .isInstanceOf(BusinessRuleException.class);
    }
}
