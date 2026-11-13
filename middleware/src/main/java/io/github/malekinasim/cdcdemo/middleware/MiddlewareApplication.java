package io.github.malekinasim.cdcdemo.middleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kafka Streams middleware: translates CDC row changes, builds the read model with joins and aggregation, and syncs order-service and shipping-service without loops.
 */
@SpringBootApplication
public class MiddlewareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiddlewareApplication.class, args);
    }
}
