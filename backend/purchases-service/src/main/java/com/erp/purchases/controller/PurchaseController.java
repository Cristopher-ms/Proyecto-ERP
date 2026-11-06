package com.erp.purchases.controller;

import com.erp.purchases.dto.CreatePurchaseRequest;
import com.erp.purchases.dto.PurchaseOrderResponse;
import com.erp.purchases.dto.PurchasesSummaryResponse;
import com.erp.purchases.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Órdenes de Compra", description = "Gestión del ciclo de vida de órdenes de compra")
@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "Listar todas las órdenes de compra")
    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchases());
    }

    @Operation(summary = "Obtener OC por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }

    @Operation(summary = "Crear nueva OC (estado DRAFT)")
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody CreatePurchaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderService.createPurchase(request));
    }

    @Operation(summary = "Aprobar OC (DRAFT → APPROVED)")
    @PutMapping("/{id}/approve")
    public ResponseEntity<PurchaseOrderResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.approvePurchase(id));
    }

    @Operation(summary = "Marcar OC como recibida (APPROVED → RECEIVED) y reponer stock")
    @PutMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receive(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.receivePurchase(id));
    }

    @Operation(summary = "Resumen/KPIs de compras")
    @GetMapping("/summary")
    public ResponseEntity<PurchasesSummaryResponse> getSummary() {
        return ResponseEntity.ok(purchaseOrderService.getSummary());
    }
}
