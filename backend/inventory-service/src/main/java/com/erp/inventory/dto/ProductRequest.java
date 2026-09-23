package com.erp.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "El código SKU es obligatorio")
    @Size(min = 3, max = 50, message = "El SKU debe tener entre 3 y 50 caracteres")
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "El precio de costo no puede ser negativo")
    private BigDecimal costPrice;

    @Builder.Default
    @Min(value = 0, message = "El stock inicial no puede ser negativo")
    private Integer stockQuantity = 0;

    @Builder.Default
    @Min(value = 0, message = "El umbral de stock mínimo no puede ser negativo")
    private Integer minStockAlert = 5;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @Builder.Default
    private Boolean active = true;
}
