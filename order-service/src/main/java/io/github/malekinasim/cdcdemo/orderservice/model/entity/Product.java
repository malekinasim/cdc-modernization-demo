package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name ="PRODUCT",uniqueConstraints = {
        @UniqueConstraint(name = "uk_stockKeepingUnit",columnNames = {"stockKeepingUnit"})})
@Getter
@Setter
public class Product extends BaseEntity<Long> {
    @Column(name = "PROD_NM" , nullable = false)
    private String productName;
    @Column(name = "UNIT_PRICE_ORE" , nullable = false)
    private Float unitPrice;
    @Column(name = "SKU" , nullable = false,unique = true)
    private String stockKeepingUnit;
}