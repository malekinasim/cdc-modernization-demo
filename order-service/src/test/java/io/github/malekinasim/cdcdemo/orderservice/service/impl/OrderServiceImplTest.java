package io.github.malekinasim.cdcdemo.orderservice.service.impl;

import io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException;
import io.github.malekinasim.cdcdemo.orderservice.exception.OrderRuleViolationException;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.CreateOrderRequest;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderDto;
import io.github.malekinasim.cdcdemo.orderservice.model.dto.OrderItemDto;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Customer;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Order;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.OrderLine;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Product;
import io.github.malekinasim.cdcdemo.orderservice.repository.OrderRepository;
import io.github.malekinasim.cdcdemo.orderservice.service.CustomerService;
import io.github.malekinasim.cdcdemo.orderservice.service.OrderLineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The order lifecycle rules, with the repositories mocked: no Spring context, no database. */
class OrderServiceImplTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-11-16T10:15:00Z"), ZoneId.of("Europe/Stockholm"));

    private OrderRepository orderRepository;
    private CustomerService customerService;
    private OrderLineService orderLineService;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        customerService = mock(CustomerService.class);
        orderLineService = mock(OrderLineService.class);
        orderService = new OrderServiceImpl(orderRepository, customerService, orderLineService, FIXED_CLOCK);

        when(orderRepository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void newOrderIsOpenWithAGeneratedNumberAndTodaysDate() {
        when(customerService.getById(7L)).thenReturn(customer());
        when(orderRepository.nextOrderNumberSequence()).thenReturn(123L);

        OrderDto order = orderService.createOrder(new CreateOrderRequest(7L));

        assertThat(order.orderNumber()).isEqualTo("ORD-000123");
        assertThat(order.status()).isEqualTo(Order.STATUS_OPEN);
        assertThat(order.orderDate()).isEqualTo("20261116");
        assertThat(order.customerName()).isEqualTo("Sara Lind");
        assertThat(order.totalOre()).isZero();
    }

    @Test
    void addingALineCopiesTheProductPriceAndUpdatesTheTotal() {
        existingOrder(openOrder());
        when(orderLineService.newLine(5L, 2)).thenReturn(line(14900, 2));

        OrderDto order = orderService.addLine("ORD-000001", new OrderItemDto(5L, 2));

        assertThat(order.lines()).singleElement()
                .satisfies(line -> {
                    assertThat(line.sku()).isEqualTo("MUG-500ML");
                    assertThat(line.unitPriceOre()).isEqualTo(14900);
                });
        assertThat(order.totalOre()).isEqualTo(29800);
    }

    @Test
    void anEmptyOrderCannotBePlaced() {
        existingOrder(openOrder());

        assertThatThrownBy(() -> orderService.placeOrder("ORD-000001"))
                .isInstanceOf(OrderRuleViolationException.class)
                .hasMessageContaining("no lines");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void anOrderWithLinesIsPlaced() {
        Order order = openOrder();
        order.addLine(line(7900, 1));
        existingOrder(order);

        assertThat(orderService.placeOrder("ORD-000001").status()).isEqualTo(Order.STATUS_PLACED);
    }

    @Test
    void linesCannotBeAddedAfterTheOrderIsPlaced() {
        Order order = openOrder();
        order.setStatus(Order.STATUS_PLACED);
        existingOrder(order);

        assertThatThrownBy(() -> orderService.addLine("ORD-000001", new OrderItemDto(5L, 1)))
                .isInstanceOf(OrderRuleViolationException.class);
    }

    @Test
    void aShippedOrderCannotBeCancelled() {
        Order order = openOrder();
        order.setStatus(Order.STATUS_SHIPPED);
        existingOrder(order);

        assertThatThrownBy(() -> orderService.cancelOrder("ORD-000001"))
                .isInstanceOf(OrderRuleViolationException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void cancellingAnAlreadyCancelledOrderChangesNothing() {
        Order order = openOrder();
        order.setStatus(Order.STATUS_CANCELLED);
        existingOrder(order);

        assertThat(orderService.cancelOrder("ORD-000001").status()).isEqualTo(Order.STATUS_CANCELLED);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void anUnknownOrderNumberIsNotFound() {
        when(orderRepository.findDetailByOrderNumber("ORD-999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder("ORD-999999"))
                .isInstanceOf(NotFoundException.class);
    }

    private void existingOrder(Order order) {
        when(orderRepository.findByOrderNumber(order.getOrderNumber())).thenReturn(Optional.of(order));
    }

    private static Order openOrder() {
        Order order = new Order();
        order.setOrderNumber("ORD-000001");
        order.setCustomer(customer());
        order.setStatus(Order.STATUS_OPEN);
        order.setOrderDate("20261116");
        return order;
    }

    private static Customer customer() {
        Customer customer = new Customer();
        customer.setId(7L);
        customer.setFirstName("Sara");
        customer.setLastName("Lind");
        customer.setEmail("sara.lind@example.com");
        customer.setCity("Stockholm");
        return customer;
    }

    private static OrderLine line(int unitPriceOre, int quantity) {
        Product product = new Product();
        product.setId(5L);
        product.setStockKeepingUnit("MUG-500ML");
        product.setProductName("Coffee mug 500 ml");
        product.setUnitPriceOre(unitPriceOre);

        OrderLine line = new OrderLine();
        line.setProduct(product);
        line.setQuantity(quantity);
        line.setUnitPriceOre(unitPriceOre);
        return line;
    }
}
