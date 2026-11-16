package io.github.malekinasim.cdcdemo.orderservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CUSTOMER", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_email", columnNames = {"EMAIL"})})
@Getter
@Setter
public class Customer extends BaseEntity {

    @Column(name = "FIRST_NM", nullable = false, length = 50)
    private String firstName;

    @Column(name = "LAST_NM", nullable = false, length = 50)
    private String lastName;

    @Column(name = "EMAIL", nullable = false, length = 120)
    private String email;

    @Column(name = "CITY", length = 60)
    private String city;

    public String fullName() {
        return firstName + " " + lastName;
    }
}
