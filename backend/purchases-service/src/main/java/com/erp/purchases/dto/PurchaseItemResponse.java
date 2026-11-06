package com.erp.purchases.dto;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Long id,
        Long productId,
        String productSku,
        String productName,
        BigDecimal unitCost,
        Integer quantity,
        BigDecimal subtotal
) {}
