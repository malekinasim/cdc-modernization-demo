package io.github.malekinasim.cdcdemo.orderservice.service.impl;

import io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException;
import io.github.malekinasim.cdcdemo.orderservice.exception.OrderRuleViolationException;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.CreateOrderRequest;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderItemDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderLineDto;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Order;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.OrderLine;
import io.github.malekinasim.cdcdemo.orderservice.repository.OrderRepository;
import io.github.malekinasim.cdcdemo.orderservice.service.CustomerService;
import io.github.malekinasim.cdcdemo.orderservice.service.OrderLineService;
import io.github.malekinasim.cdcdemo.orderservice.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The order lifecycle, written the way an older service would be: plain JPA, no events.
 * Nothing here knows about Kafka; CDC picks every change up from the database.
 */
@Service
public class OrderServiceImpl implements OrderService {

    /** The legacy format of ORD_DT: a string, not a date. */
    private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final OrderLineService orderLineService;
    private final Clock clock;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerService customerService,
                            OrderLineService orderLineService,
                            Clock clock) {
        this.orderRepository = orderRepository;
        this.customerService = customerService;
        this.orderLineService = orderLineService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomer(customerService.getById(request.customerId()));
        order.setOrderNumber(nextOrderNumber());
        order.setStatus(Order.STATUS_OPEN);
        order.setOrderDate(LocalDate.now(clock).format(ORDER_DATE_FORMAT));

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto addLine(String orderNumber, OrderItemDto request) {
        Order order = requireOrderForUpdate(orderNumber);
        if (order.getStatus() != Order.STATUS_OPEN) {
            throw new OrderRuleViolationException(
                    "Order " + orderNumber + " is in status " + order.getStatus()
                            + "; lines can only be added while it is open");
        }

        OrderLine line = orderLineService.newLine(request.productId(), request.quantity());
        order.addLine(line);

        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto placeOrder(String orderNumber) {
        Order order = requireOrderForUpdate(orderNumber);
        if (order.getStatus() != Order.STATUS_OPEN) {
            throw new OrderRuleViolationException(
                    "Only an open order can be placed; order " + orderNumber
                            + " is in status " + order.getStatus());
        }
        if (order.getLines().isEmpty()) {
            throw new OrderRuleViolationException("Order " + orderNumber + " has no lines");
        }

        order.setStatus(Order.STATUS_PLACED);
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(String orderNumber) {
        Order order = requireOrderForUpdate(orderNumber);
        if (order.getStatus() == Order.STATUS_SHIPPED || order.getStatus() == Order.STATUS_DELIVERED) {
            throw new OrderRuleViolationException(
                    "Order " + orderNumber + " has already left the warehouse (status "
                            + order.getStatus() + ") and cannot be cancelled");
        }
        if (order.getStatus() == Order.STATUS_CANCELLED) {
            return toDto(order);
        }

        order.setStatus(Order.STATUS_CANCELLED);
        return toDto(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrder(String orderNumber) {
        return toDto(orderRepository.findDetailByOrderNumber(orderNumber)
                .orElseThrow(() -> notFound(orderNumber)));
    }

    /**
     * Loads an order for a change, locking its row until the transaction commits, so the status
     * check and the write that follows it cannot be interleaved with another request.
     */
    private Order requireOrderForUpdate(String orderNumber) {
        return orderRepository.findByOrderNumberForUpdate(orderNumber)
                .orElseThrow(() -> notFound(orderNumber));
    }

    private static NotFoundException notFound(String orderNumber) {
        return new NotFoundException("No order with number " + orderNumber);
    }

    private String nextOrderNumber() {
        return "ORD-%06d".formatted(orderRepository.nextOrderNumberSequence());
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
