package io.github.malekinasim.cdcdemo.orderservice.service;

import io.github.malekinasim.cdcdemo.orderservice.model.entity.Customer;

import java.util.List;

public interface CustomerService {

    /** @throws io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException if there is no such customer */
    Customer getById(Long id);

    List<Customer> findAll();
}
