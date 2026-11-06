package com.erp.purchases.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${erp.services.inventory-url:http://localhost:8082}")
    private String inventoryUrl;

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "addStockFallback")
    @Retry(name = "inventoryService")
    public void addStock(Long productId, int quantity, String purchaseNumber) {
        log.info("Llamando a inventory-service para reponer {} unidades del producto ID {} — Ref: {}",
                quantity, productId, purchaseNumber);

        webClientBuilder.build()
                .post()
                .uri(inventoryUrl + "/api/v1/products/{id}/add-stock", productId)
                .bodyValue(Map.of(
                        "quantity", quantity,
                        "referenceId", purchaseNumber,
                        "reason", "Reposición por Orden de Compra"
                ))
                .retrieve()
                .toBodilessEntity()
                .block();

        log.info("Stock repuesto exitosamente para producto ID {} — {} unidades.", productId, quantity);
    }

    // Fallback resiliente: si inventory-service no responde, el flujo continúa sin bloquear
    public void addStockFallback(Long productId, int quantity, String purchaseNumber, Exception ex) {
        log.warn("[Circuit Breaker ABIERTO] No se pudo contactar inventory-service para producto ID {}. " +
                "Reposición en cola para reintento. Error: {}", productId, ex.getMessage());
    }
}
