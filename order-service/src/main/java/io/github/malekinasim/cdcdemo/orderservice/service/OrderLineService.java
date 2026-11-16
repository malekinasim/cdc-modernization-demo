package io.github.malekinasim.cdcdemo.orderservice.service;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.OrderLine;

public interface OrderLineService {

    /**
     * Builds a line for the given product, copying the product's current price into the line so a
     * later price change does not rewrite past orders. The line is not attached to an order yet.
     */
    OrderLine newLine(Long productId, int quantity);
}
