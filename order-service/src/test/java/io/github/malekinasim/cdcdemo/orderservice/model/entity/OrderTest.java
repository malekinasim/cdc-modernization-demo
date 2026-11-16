package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plain unit tests: no Spring context, so no database is needed.
 * The integration tests against real PostgreSQL and Kafka come with Testcontainers later.
 */
class OrderTest {

    @Test
    void newOrderStartsOpenAndEmpty() {
        Order order = new Order();

        assertThat(order.getStatus()).isEqualTo(Order.STATUS_OPEN);
        assertThat(order.getLines()).isEmpty();
        assertThat(order.totalOre()).isZero();
    }

    @Test
    void totalIsTheSumOfQuantityTimesLinePrice() {
        Order order = new Order();
        order.addLine(line(14900, 2));
        order.addLine(line(7900, 3));

        assertThat(order.totalOre()).isEqualTo(14900 * 2 + 7900 * 3);
    }

    @Test
    void addingALineLinksItBackToTheOrder() {
        Order order = new Order();
        OrderLine line = line(12900, 1);

        order.addLine(line);

        assertThat(line.getOrder()).isSameAs(order);
        assertThat(order.getLines()).containsExactly(line);
    }

    private static OrderLine line(int unitPriceOre, int quantity) {
        OrderLine line = new OrderLine();
        line.setUnitPriceOre(unitPriceOre);
        line.setQuantity(quantity);
        return line;
    }
}
