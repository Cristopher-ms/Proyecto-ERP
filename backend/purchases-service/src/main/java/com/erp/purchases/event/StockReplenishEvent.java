package com.erp.purchases.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockReplenishEvent implements Serializable {
    private Long purchaseId;
    private String purchaseNumber;
    private Long supplierId;
    private String supplierName;
    private List<StockReplenishItemEvent> items;
    private BigDecimal totalAmount;
    private LocalDateTime receivedAt;

    public StockReplenishEvent(String purchaseNumber, Long supplierId, List<StockReplenishItemEvent> items) {
        this.purchaseNumber = purchaseNumber;
        this.supplierId = supplierId;
        this.items = items;
        this.receivedAt = LocalDateTime.now();
    }
}
