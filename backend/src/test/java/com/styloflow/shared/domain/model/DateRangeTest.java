package com.styloflow.shared.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.styloflow.shared.domain.exception.BusinessRuleException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Date range in the business time zone")
class DateRangeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 2);
    private static final ZoneId LA_PAZ = ZoneId.of("America/La_Paz");

    @Test
    void of_should_defaultToToday_when_noDates() {
        assertThat(DateRange.of(null, null, TODAY)).isEqualTo(new DateRange(TODAY, TODAY));
    }

    @Test
    void of_should_useFromAsTo_when_toMissing() {
        LocalDate from = TODAY.minusDays(3);

        assertThat(DateRange.of(from, null, TODAY)).isEqualTo(new DateRange(from, from));
    }

    @Test
    void new_should_throw_when_toBeforeFrom() {
        assertThatThrownBy(() -> new DateRange(TODAY, TODAY.minusDays(1))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void startAndEnd_should_coverWholeDaysInZone() {
        DateRange range = new DateRange(TODAY, TODAY);

        assertThat(range.start(LA_PAZ)).isEqualTo(Instant.parse("2026-10-02T04:00:00Z"));
        assertThat(range.end(LA_PAZ)).isEqualTo(Instant.parse("2026-10-03T04:00:00Z"));
    }
}
