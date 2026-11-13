package io.github.malekinasim.cdcdemo.queryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CQRS read side: builds a denormalized read model from the canonical events and serves queries.
 */
@SpringBootApplication
public class QueryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryServiceApplication.class, args);
    }
}
