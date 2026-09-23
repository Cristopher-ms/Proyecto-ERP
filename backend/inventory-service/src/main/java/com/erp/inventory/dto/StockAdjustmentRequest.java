package com.erp.inventory.dto;

import com.erp.inventory.domain.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "El tipo de movimiento es obligatorio (IN, OUT, ADJUSTMENT)")
    private MovementType type;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser como mínimo 1")
    private Integer quantity;

    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    private String reason;

    @Size(max = 100, message = "La referencia no puede exceder 100 caracteres")
    private String referenceId;
}
