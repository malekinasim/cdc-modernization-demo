package io.github.malekinasim.cdcdemo.orderservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Request to add a line to an open order. */
public record OrderItemDto(

        @NotNull
        Long productId,

        @NotNull
        @Min(1)
        Integer quantity) {
}
