package com.erp.purchases.dto;

import com.erp.purchases.domain.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String purchaseNumber,
        SupplierResponse supplier,
        PurchaseStatus status,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        List<PurchaseItemResponse> items,
        LocalDateTime expectedDeliveryDate,
        LocalDateTime receivedDate,
        String notes,
        String buyerEmail,
        LocalDateTime createdAt
) {}
