package io.github.malekinasim.cdcdemo.orderservice.controller;

import io.github.malekinasim.cdcdemo.orderservice.model.dto.CreateOrderRequest;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderItemDto;
import io.github.malekinasim.cdcdemo.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                UriComponentsBuilder uriBuilder) {
        OrderDto order = orderService.createOrder(request);
        return ResponseEntity
                .created(uriBuilder.path("/api/orders/{orderNo}").build(order.orderNumber()))
                .body(order);
    }

    @PostMapping("/{orderNo}/lines")
    public ResponseEntity<OrderDto> addLine(@PathVariable("orderNo") String orderNumber,
                                            @Valid @RequestBody OrderItemDto request) {
        return ResponseEntity.ok(orderService.addLine(orderNumber, request));
    }

    @PatchMapping("/{orderNo}/place")
    public ResponseEntity<OrderDto> placeOrder(@PathVariable("orderNo") String orderNumber) {
        return ResponseEntity.ok(orderService.placeOrder(orderNumber));
    }

    @PatchMapping("/{orderNo}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable("orderNo") String orderNumber) {
        return ResponseEntity.ok(orderService.cancelOrder(orderNumber));
    }

    @GetMapping("/{orderNo}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("orderNo") String orderNumber) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }
}
