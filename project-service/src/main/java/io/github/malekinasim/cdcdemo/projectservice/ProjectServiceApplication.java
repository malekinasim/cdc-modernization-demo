package io.github.malekinasim.cdcdemo.projectservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application (command side): projects, tasks, plans and monthly progress in a normalized PostgreSQL model. It knows nothing about Kafka.
 */
@SpringBootApplication
public class ProjectServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectServiceApplication.class, args);
    }
}
