package io.github.malekinasim.cdcdemo.orderservice.model.dto;

import java.util.List;

/** An order as returned to the client. */
public record OrderDto(
        String orderNumber,
        Long customerId,
        String customerName,
        Character status,
        String orderDate,
        Integer totalOre,
        List<OrderLineDto> lines) {
}
