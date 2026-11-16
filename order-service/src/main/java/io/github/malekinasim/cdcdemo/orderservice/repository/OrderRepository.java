package io.github.malekinasim.cdcdemo.orderservice.repository;


import io.github.malekinasim.cdcdemo.orderservice.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
