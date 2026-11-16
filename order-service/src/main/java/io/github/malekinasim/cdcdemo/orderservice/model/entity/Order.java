package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * An order. The table is called ORDERS because ORDER is a reserved SQL keyword.
 *
 * <p>Status codes, like the date stored as a string, are deliberately legacy-style: translating
 * them into a typed read model is part of what the middleware does.
 */
@Entity
@Table(name = "ORDERS", uniqueConstraints = {
        @UniqueConstraint(name = "uk_orders_ord_no", columnNames = {"ORD_NO"})})
@Getter
@Setter
public class Order extends BaseEntity {

    public static final char STATUS_OPEN = 'O';
    public static final char STATUS_PLACED = 'P';
    public static final char STATUS_CANCELLED = 'X';
    public static final char STATUS_SHIPPED = 'S';
    public static final char STATUS_DELIVERED = 'D';

    @Column(name = "ORD_NO", nullable = false, length = 20)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUST_ID", nullable = false)
    private Customer customer;

    @Column(name = "STATUS_CD", nullable = false)
    private Character status = STATUS_OPEN;

    /** Order date as 'yyyyMMdd', the way the legacy system stored it. */
    @Column(name = "ORD_DT", nullable = false, length = 8)
    private String orderDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    public void addLine(OrderLine line) {
        line.setOrder(this);
        lines.add(line);
    }

    public int totalOre() {
        return lines.stream()
                .mapToInt(line -> line.getUnitPriceOre() * line.getQuantity())
                .sum();
    }
}
