package io.github.malekinasim.cdcdemo.orderservice.service.impl;

import io.github.malekinasim.cdcdemo.orderservice.exception.NotFoundException;
import io.github.malekinasim.cdcdemo.orderservice.model.entity.Customer;
import io.github.malekinasim.cdcdemo.orderservice.repository.CustomerRepository;
import io.github.malekinasim.cdcdemo.orderservice.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No customer with id " + id));
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
