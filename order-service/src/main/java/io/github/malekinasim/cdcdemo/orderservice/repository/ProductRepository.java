package io.github.malekinasim.cdcdemo.orderservice.repository;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByStockKeepingUnit(String stockKeepingUnit);
}
