package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_LINE")
@Getter
@Setter
public class OrderLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORD_ID", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PROD_ID", nullable = false)
    private Product product;

    @Column(name = "QTY", nullable = false)
    private Integer quantity;

    /** The product price copied at the time the line was added, in öre. */
    @Column(name = "UNIT_PRICE_ORE", nullable = false)
    private Integer unitPriceOre;
}
