package io.github.malekinasim.cdcdemo.actionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The new action service: own database, inbox table for idempotency, outbox for publishing its own changes.
 */
@SpringBootApplication
public class ActionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActionServiceApplication.class, args);
    }
}
