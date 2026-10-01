package com.styloflow.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    /** Zona horaria del negocio: define qué es "hoy" para caja y reportes. */
    @Bean
    ZoneId zonaNegocio(AppProperties props) {
        return ZoneId.of(props.zonaHoraria());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
