package com.travel.booking.service;

import com.travel.booking.entity.Customer;
import com.travel.booking.exception.ResourceNotFoundException;
import com.travel.booking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    public Customer create(Customer customer) {
        customer.setCustomerId(null);
        return customerRepository.save(customer);
    }

    public Customer update(Long id, Customer updated) {
        Customer existing = findById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setCity(updated.getCity());
        return customerRepository.save(existing);
    }

    public void delete(Long id) {
        customerRepository.delete(findById(id));
    }
}
