package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_sku", columnNames = {"SKU"})})
@Getter
@Setter
public class Product extends BaseEntity {

    /** Stock keeping unit: the code the warehouse and the read model identify a product by. */
    @Column(name = "SKU", nullable = false, length = 40)
    private String stockKeepingUnit;

    @Column(name = "PROD_NM", nullable = false, length = 120)
    private String productName;

    /** Price in öre (1/100 SEK), as an integer. Never a floating point type for money. */
    @Column(name = "UNIT_PRICE_ORE", nullable = false)
    private Integer unitPriceOre;
}
