package io.github.malekinasim.cdcdemo.orderservice.service.impl;

import io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Product;
import io.github.malekinasim.cdcdemo.orderservice.repository.ProductRepository;
import io.github.malekinasim.cdcdemo.orderservice.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No product with id " + id));
    }

    @Override
    public Product getBySku(String sku) {
        return productRepository.findByStockKeepingUnit(sku)
                .orElseThrow(() -> new NotFoundException("No product with SKU " + sku));
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
