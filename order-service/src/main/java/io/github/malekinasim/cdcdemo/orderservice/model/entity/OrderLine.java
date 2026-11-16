package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.*;
import jakarta.persistence.criteria.Order;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name ="ORDER_LINE",uniqueConstraints = {
        @UniqueConstraint(name = "uk_email",columnNames = {"email"})})
@Getter
@Setter
public class OrderLine extends BaseEntity<Long>{
    @ManyToOne
    @JoinColumn(name = "ORD_ID")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "PROD_ID")
    private Product product;
    @Column(name = "QTY")
    private Integer Quantity;
    @Column(name = "UNIT_PRICE_ORE")
    private Float unitPrice;
}
