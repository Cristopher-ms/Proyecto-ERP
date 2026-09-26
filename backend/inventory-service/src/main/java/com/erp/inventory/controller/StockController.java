package com.erp.inventory.controller;

import com.erp.inventory.dto.PaginatedResponse;
import com.erp.inventory.dto.StockAdjustmentRequest;
import com.erp.inventory.dto.StockMovementResponse;
import com.erp.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Stock", description = "Endpoints para registro de entradas, salidas y auditoría de inventario")
public class StockController {

    private final StockService stockService;

    @PostMapping("/adjust")
    @Operation(summary = "Registrar un ajuste manual de stock (entrada, salida o calibración)")
    public ResponseEntity<StockMovementResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockService.adjustStock(request));
    }

    @GetMapping("/movements")
    @Operation(summary = "Consultar el historial paginado de movimientos de inventario")
    public ResponseEntity<PaginatedResponse<StockMovementResponse>> getAllMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return ResponseEntity.ok(stockService.getAllMovements(page, size));
    }

    @GetMapping("/movements/product/{productId}")
    @Operation(summary = "Consultar el historial de movimientos de un producto específico")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getMovementsByProduct(productId));
    }
}
