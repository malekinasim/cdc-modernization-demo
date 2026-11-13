package io.github.malekinasim.cdcdemo.legacyapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The legacy monolith: a deliberately old-style Spring Boot app on its own PostgreSQL database. Source of CDC events.
 */
@SpringBootApplication
public class LegacyAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyAppApplication.class, args);
    }
}
