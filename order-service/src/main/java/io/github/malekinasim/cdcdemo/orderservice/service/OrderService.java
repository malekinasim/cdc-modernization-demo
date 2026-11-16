package io.github.malekinasim.cdcdemo.orderservice.service;

import io.github.malekinasim.cdcdemo.orderservice.model.dto.CreateOrderRequest;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderItemDto;

public interface OrderService {

    /** Opens a new order in status O for the given customer. */
    OrderDto createOrder(CreateOrderRequest request);

    /** Adds a line to an open order. Only allowed while the order is in status O. */
    OrderDto addLine(String orderNumber, OrderItemDto request);

    /** Places an order: O to P. An order without lines cannot be placed. */
    OrderDto placeOrder(String orderNumber);

    /** Cancels an order: O or P to X. An order already shipped cannot be cancelled. */
    OrderDto cancelOrder(String orderNumber);

    OrderDto getOrder(String orderNumber);
}
