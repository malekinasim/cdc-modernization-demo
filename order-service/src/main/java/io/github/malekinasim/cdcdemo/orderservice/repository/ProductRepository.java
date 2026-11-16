package io.github.malekinasim.cdcdemo.orderservice.repository;


import io.github.malekinasim.cdcdemo.orderservice.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
}
