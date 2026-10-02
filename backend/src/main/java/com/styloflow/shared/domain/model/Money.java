package com.styloflow.shared.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.function.Function;

/** Amounts always use scale 2 (NUMERIC(12,2)). */
public final class Money {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private Money() {}

    public static BigDecimal of(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal ofOrZero(BigDecimal value) {
        return value != null ? of(value) : ZERO;
    }

    public static <T> BigDecimal sum(Collection<T> items, Function<T, BigDecimal> amount) {
        return items.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
