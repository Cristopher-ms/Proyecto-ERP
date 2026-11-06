package com.erp.purchases.controller;

import com.erp.purchases.dto.SupplierRequest;
import com.erp.purchases.dto.SupplierResponse;
import com.erp.purchases.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Proveedores", description = "Gestión del directorio de proveedores")
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "Listar todos los proveedores")
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(supplierService.getAllSuppliers(search));
    }

    @Operation(summary = "Obtener proveedor por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @Operation(summary = "Crear nuevo proveedor")
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }
}
