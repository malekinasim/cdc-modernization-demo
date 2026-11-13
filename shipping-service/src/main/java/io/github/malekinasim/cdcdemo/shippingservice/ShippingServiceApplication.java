package io.github.malekinasim.cdcdemo.shippingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Shipping service: a second system with its own model (shipments). Kept in sync with order-service in both directions.
 */
@SpringBootApplication
public class ShippingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}
