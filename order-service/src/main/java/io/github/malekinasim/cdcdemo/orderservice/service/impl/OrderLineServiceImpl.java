package io.github.malekinasim.cdcdemo.orderservice.service.impl;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.OrderLine;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Product;
import io.github.malekinasim.cdcdemo.orderservice.service.OrderLineService;
import io.github.malekinasim.cdcdemo.orderservice.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class OrderLineServiceImpl implements OrderLineService {

    private final ProductService productService;

    public OrderLineServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public OrderLine newLine(Long productId, int quantity) {
        Product product = productService.getById(productId);

        OrderLine line = new OrderLine();
        line.setProduct(product);
        line.setQuantity(quantity);
        line.setUnitPriceOre(product.getUnitPriceOre());
        return line;
    }
}
