package com.styloflow.shared.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    /** Business time zone: defines what "today" means for the cash register and reports. */
    @Bean
    ZoneId businessZone(AppProperties props) {
        return ZoneId.of(props.timeZone());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
