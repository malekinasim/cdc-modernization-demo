package io.github.malekinasim.cdcdemo.orderservice.model.dto;

/** One line of an order, as returned to the client. */
public record OrderLineDto(
        String sku,
        String productName,
        Integer quantity,
        Integer unitPriceOre) {
}
