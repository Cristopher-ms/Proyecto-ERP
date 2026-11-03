package com.erp.orders.service;

import com.erp.orders.domain.entity.Customer;
import com.erp.orders.domain.repository.CustomerRepository;
import com.erp.orders.dto.CustomerRequest;
import com.erp.orders.dto.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(String search) {
        List<Customer> list;
        if (search != null && !search.trim().isEmpty()) {
            list = customerRepository.searchCustomers(search.trim());
        } else {
            list = customerRepository.findByActiveTrueOrderByFirstNameAsc();
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        customerRepository.findByDocumentNumber(request.getDocumentNumber().trim())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe un cliente con el documento: " + request.getDocumentNumber());
                });

        Customer customer = Customer.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber().trim())
                .address(request.getAddress())
                .active(true)
                .build();

        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .documentType(customer.getDocumentType())
                .documentNumber(customer.getDocumentNumber())
                .address(customer.getAddress())
                .active(customer.getActive())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
