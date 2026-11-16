package io.github.malekinasim.cdcdemo.orderservice.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request to add a line to an open order. */
public record OrderItemDto(

        @NotNull
        Long productId,

        /** Bounded on purpose: a line of a web shop order is not a bulk contract. */
        @NotNull
        @Min(1)
        @Max(1000)
        Integer quantity) {
}
