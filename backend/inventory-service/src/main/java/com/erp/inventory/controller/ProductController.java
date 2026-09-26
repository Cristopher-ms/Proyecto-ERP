package com.erp.inventory.controller;

import com.erp.inventory.dto.InventorySummaryResponse;
import com.erp.inventory.dto.PaginatedResponse;
import com.erp.inventory.dto.ProductRequest;
import com.erp.inventory.dto.ProductResponse;
import com.erp.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Productos e Inventario", description = "Endpoints para el catálogo de productos y control de existencias")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar productos con paginación, búsqueda por SKU/nombre y filtro de stock bajo")
    public ResponseEntity<PaginatedResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean lowStockOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        PaginatedResponse<ProductResponse> products = productService.getProducts(
                search, categoryId, lowStockOnly, page, size, sortBy, direction
        );
        return ResponseEntity.ok(products);
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener resumen y KPIs de inventario para el dashboard")
    public ResponseEntity<InventorySummaryResponse> getSummary() {
        return ResponseEntity.ok(productService.getInventorySummary());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un producto por ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Buscar un producto por código SKU")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo producto con stock inicial")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar información de un producto")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar un producto (baja lógica)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
