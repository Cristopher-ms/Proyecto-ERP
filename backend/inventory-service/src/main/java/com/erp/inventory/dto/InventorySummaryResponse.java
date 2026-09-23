package com.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryResponse {
    private long totalProducts;
    private long lowStockAlerts;
    private long totalUnitsInStock;
    private BigDecimal totalValuation;
}
