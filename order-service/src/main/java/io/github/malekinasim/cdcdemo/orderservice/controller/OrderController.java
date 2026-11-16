package io.github.malekinasim.cdcdemo.orderservice.controller;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderItemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<> createOrder(@Validated @RequestBody OrderDto orderDto){

    }

    @PostMapping(value = "/{orderNo}/lines")
    public ResponseEntity<> createOrderItem(@PathVariable(value = "orderNo") String orderNumber, @Validated @RequestBody OrderItemDto orderItemDto){

    }
    @PostMapping(value = "/{orderNo}/lines")
    public ResponseEntity<> createOrder(@PathVariable(value = "orderNo") String orderNumber, @Validated @RequestBody OrderItemDto orderItemDto){

    }
    @PatchMapping(value = "/{orderNo}/place")
    public ResponseEntity<> placeOrder(@PathVariable(value = "orderNo") String orderNumber){

    }
    @PatchMapping(value = "/{orderNo}/cancle")
    public ResponseEntity<> cancelOrder(@PathVariable(value = "orderNo") String orderNumber){

    }

    @GetMapping(value = "/orders/{orderNo}")
    public ResponseEntity<> cancelOrder(@PathVariable(value = "orderNo") String orderNumber){

    }
}
