package io.github.malekinasim.cdcdemo.orderservice.repository;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}
