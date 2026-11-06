package com.erp.purchases.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseItemRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productId,

        @NotBlank(message = "El SKU es obligatorio")
        String productSku,

        @NotBlank(message = "El nombre del producto es obligatorio")
        String productName,

        @NotNull(message = "El costo unitario es obligatorio")
        @DecimalMin(value = "0.01", message = "El costo debe ser mayor a cero")
        BigDecimal unitCost,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        Integer quantity
) {
    public Long getProductId() { return productId; }
    public String getProductSku() { return productSku; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitCost() { return unitCost; }
    public Integer getQuantity() { return quantity; }
}
