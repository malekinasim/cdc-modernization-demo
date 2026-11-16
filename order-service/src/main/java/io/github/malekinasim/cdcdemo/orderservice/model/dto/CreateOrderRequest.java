package io.github.malekinasim.cdcdemo.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;

/** Request to open a new order for a customer. */
public record CreateOrderRequest(

        @NotNull
        Long customerId) {
}
