package com.erp.orders.controller;

import com.erp.orders.dto.CustomerRequest;
import com.erp.orders.dto.CustomerResponse;
import com.erp.orders.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints para la gestión y directorio de clientes corporativos")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Listar clientes activos con búsqueda opcional por nombre o documento")
    public ResponseEntity<List<CustomerResponse>> getCustomers(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(customerService.getAllCustomers(search));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un cliente por su ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar un nuevo cliente")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }
}
