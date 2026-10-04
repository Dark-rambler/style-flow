package com.styloflow.shared.infrastructure.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    @Bean
    ZoneId businessZone(AppProperties props) {
        return ZoneId.of(props.timeZone());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
