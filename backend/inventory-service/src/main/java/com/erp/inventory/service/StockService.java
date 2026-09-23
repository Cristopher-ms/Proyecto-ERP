package com.erp.inventory.service;

import com.erp.inventory.domain.entity.Product;
import com.erp.inventory.domain.entity.StockMovement;
import com.erp.inventory.domain.enums.MovementType;
import com.erp.inventory.domain.repository.ProductRepository;
import com.erp.inventory.domain.repository.StockMovementRepository;
import com.erp.inventory.dto.PaginatedResponse;
import com.erp.inventory.dto.StockAdjustmentRequest;
import com.erp.inventory.dto.StockMovementResponse;
import com.erp.inventory.exception.InsufficientStockException;
import com.erp.inventory.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public StockMovementResponse adjustStock(StockAdjustmentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + request.getProductId()));

        int previousStock = product.getStockQuantity();
        int newStock;

        switch (request.getType()) {
            case IN -> newStock = previousStock + request.getQuantity();
            case OUT -> {
                if (previousStock < request.getQuantity()) {
                    throw new InsufficientStockException(
                            "Stock insuficiente para producto SKU '" + product.getSku() +
                            "'. Disponible: " + previousStock + ", Solicitado: " + request.getQuantity()
                    );
                }
                newStock = previousStock - request.getQuantity();
            }
            case ADJUSTMENT -> newStock = request.getQuantity();
            default -> throw new IllegalArgumentException("Tipo de movimiento desconocido: " + request.getType());
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(request.getReason() != null ? request.getReason() : "Ajuste manual de inventario")
                .referenceId(request.getReferenceId() != null ? request.getReferenceId() : "ADJUST-" + System.currentTimeMillis())
                .build();

        StockMovement savedMovement = stockMovementRepository.save(movement);
        return mapToResponse(savedMovement);
    }

    @Transactional
    public StockMovementResponse decreaseStock(Long productId, int quantity, String reason, String referenceId) {
        return adjustStock(StockAdjustmentRequest.builder()
                .productId(productId)
                .type(MovementType.OUT)
                .quantity(quantity)
                .reason(reason)
                .referenceId(referenceId)
                .build());
    }

    @Transactional
    public StockMovementResponse increaseStock(Long productId, int quantity, String reason, String referenceId) {
        return adjustStock(StockAdjustmentRequest.builder()
                .productId(productId)
                .type(MovementType.IN)
                .quantity(quantity)
                .reason(reason)
                .referenceId(referenceId)
                .build());
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<StockMovementResponse> getAllMovements(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StockMovement> movementPage = stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable);
        Page<StockMovementResponse> dtoPage = movementPage.map(this::mapToResponse);
        return PaginatedResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + productId);
        }
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private StockMovementResponse mapToResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productSku(movement.getProduct().getSku())
                .productName(movement.getProduct().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .previousStock(movement.getPreviousStock())
                .newStock(movement.getNewStock())
                .reason(movement.getReason())
                .referenceId(movement.getReferenceId())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
