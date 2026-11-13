package io.github.malekinasim.cdcdemo.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Order service (command side): customers, products, orders and order lines in a normalized PostgreSQL model. It knows nothing about Kafka.
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
