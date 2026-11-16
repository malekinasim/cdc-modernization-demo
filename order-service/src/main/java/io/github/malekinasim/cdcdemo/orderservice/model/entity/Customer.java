package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name ="CUSTOMER",uniqueConstraints = {
        @UniqueConstraint(name = "uk_email",columnNames = {"email"})})
@Getter
@Setter
public class Customer extends BaseEntity<Long> {
    @Column(name = "FIRST_NM",nullable = false)
    private String name;
    @Column(name = "LAST_NM",nullable = false)
    private String family;
    @Column(name = "EMAIL",nullable = false)
    private String email;

}
