package com.erp.purchases.event;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockReplenishItemEvent implements Serializable {
    private Long productId;
    private String productSku;
    private Integer quantity;
    private String purchaseNumber;

    public StockReplenishItemEvent(Long productId, String productSku, Integer quantity) {
        this.productId = productId;
        this.productSku = productSku;
        this.quantity = quantity;
    }
}
