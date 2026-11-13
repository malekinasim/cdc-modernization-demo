package io.github.malekinasim.cdcdemo.middleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka Streams middleware: unwraps Debezium events, maps them to canonical Avro events, routes by source and prevents sync loops.
 */
@SpringBootApplication
public class MiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiddlewareApplication.class, args);
    }
}
