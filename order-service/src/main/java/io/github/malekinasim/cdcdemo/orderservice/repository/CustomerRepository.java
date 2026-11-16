package io.github.malekinasim.cdcdemo.orderservice.repository;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
