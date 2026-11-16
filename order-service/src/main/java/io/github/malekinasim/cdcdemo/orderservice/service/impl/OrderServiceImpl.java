package io.github.malekinasim.cdcdemo.orderservice.service.impl;

import io.github.malekinasim.cdcdemo.orderservice.model.dto.CreateOrderRequest;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderItemDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderLineDto;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Order;
import io.github.malekinasim.cdcdemo.orderservice.repository.CustomerRepository;
import io.github.malekinasim.cdcdemo.orderservice.repository.OrderRepository;
import io.github.malekinasim.cdcdemo.orderservice.repository.ProductRepository;
import io.github.malekinasim.cdcdemo.orderservice.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * All the order logic, in one service class with direct repository access: the shape an older
 * monolith would have. No events are published here; CDC picks the changes up from the database.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        // TODO: load the customer, generate ORD_NO, save an order in status O
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    @Transactional
    public OrderDto addLine(String orderNumber, OrderItemDto request) {
        // TODO: only while status is O; copy the product price into the line
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    @Transactional
    public OrderDto placeOrder(String orderNumber) {
        // TODO: O -> P; reject an order with no lines
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(String orderNumber) {
        // TODO: O or P -> X; reject when already S or D
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(String orderNumber) {
        // TODO: load by order number and map
        throw new UnsupportedOperationException("not implemented yet");
    }

    static OrderDto toDto(Order order) {
        List<OrderLineDto> lines = order.getLines().stream()
                .map(line -> new OrderLineDto(
                        line.getProduct().getStockKeepingUnit(),
                        line.getProduct().getProductName(),
                        line.getQuantity(),
                        line.getUnitPriceOre()))
                .toList();

        return new OrderDto(
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().fullName(),
                order.getStatus(),
                order.getOrderDate(),
                order.totalOre(),
                lines);
    }
}
