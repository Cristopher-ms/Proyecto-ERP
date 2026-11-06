package com.erp.purchases.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseItemRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotBlank(message = "El SKU es obligatorio")
    private String productSku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El costo debe ser mayor a cero")
    private BigDecimal unitCost;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer quantity;
}
