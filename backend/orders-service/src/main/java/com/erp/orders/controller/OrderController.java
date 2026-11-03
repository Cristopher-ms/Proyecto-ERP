package com.erp.orders.controller;

import com.erp.orders.domain.enums.OrderStatus;
import com.erp.orders.dto.CreateOrderRequest;
import com.erp.orders.dto.OrderResponse;
import com.erp.orders.dto.SalesSummaryResponse;
import com.erp.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Pedidos y Ventas", description = "Endpoints para la creación, consulta y métricas de órdenes de venta")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar órdenes de venta paginadas con filtros opcionales de estado y búsqueda")
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(orderService.getOrders(status, search, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle completo de una orden por ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    @Operation(summary = "Crear y confirmar una nueva orden de venta (emite evento asíncrono para descuento de inventario)")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener métricas y KPIs de ventas en tiempo real para dashboards empresariales")
    public ResponseEntity<SalesSummaryResponse> getSalesSummary() {
        return ResponseEntity.ok(orderService.getSalesSummary());
    }
}
