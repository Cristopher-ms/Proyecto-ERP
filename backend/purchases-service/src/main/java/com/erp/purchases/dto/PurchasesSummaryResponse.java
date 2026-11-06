package com.erp.purchases.dto;

import java.math.BigDecimal;

public record PurchasesSummaryResponse(
        long totalPurchases,
        BigDecimal totalAmount,
        long pendingOrders,
        BigDecimal monthlyAmount
) {}
