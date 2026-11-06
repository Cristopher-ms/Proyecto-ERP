package com.erp.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad a reponer debe ser mínimo 1")
    private Integer quantity;

    private String referenceId;
    private String reason;
}
