package io.github.malekinasim.cdcdemo.middleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka Streams middleware: turns CDC row changes into the read model shape with translation, joins and aggregation.
 */
@SpringBootApplication
public class MiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiddlewareApplication.class, args);
    }
}
