package io.github.malekinasim.cdcdemo.orderservice.model.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name ="ORDER",uniqueConstraints = {
        @UniqueConstraint(name = "uk_orderNumber",columnNames = {"orderNumber"})})
@Getter
@Setter
public class Order extends BaseEntity<Long>{

    @Column(name = "ORD_NO" , nullable = false)
    private String orderNumber;
    @Column(name = "STATUS_CD",nullable = false)
    private Character status='O';
    @Column(name = "ORD_DT",nullable = false )
    private String orderDate;
}
