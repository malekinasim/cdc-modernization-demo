package io.github.malekinasim.cdcdemo.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

    /** Injected rather than called statically, so tests can fix the order date. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
