package io.github.malekinasim.cdcdemo.orderservice.service;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.Product;

import java.util.List;

public interface ProductService {

    /** @throws io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException if there is no such product */
    Product getById(Long id);

    /** @throws io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException if there is no such SKU */
    Product getBySku(String sku);

    List<Product> findAll();
}
