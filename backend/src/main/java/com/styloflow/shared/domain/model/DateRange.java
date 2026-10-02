package com.styloflow.shared.domain.model;

import com.styloflow.shared.domain.exception.BusinessRuleException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Inclusive range of days, interpreted in the business time zone. */
public record DateRange(LocalDate from, LocalDate to) {

    public DateRange {
        if (to.isBefore(from)) {
            throw new BusinessRuleException("The 'to' date cannot be before the 'from' date");
        }
    }

    /** No dates = today; no {@code to} = the same day as {@code from}. */
    public static DateRange of(LocalDate from, LocalDate to, LocalDate today) {
        LocalDate start = from != null ? from : today;
        return new DateRange(start, to != null ? to : start);
    }

    public Instant start(ZoneId zone) {
        return from.atStartOfDay(zone).toInstant();
    }

    /** Exclusive bound: start of the day after {@code to}. */
    public Instant end(ZoneId zone) {
        return to.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
