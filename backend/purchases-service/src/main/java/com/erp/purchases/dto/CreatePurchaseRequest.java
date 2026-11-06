package com.erp.purchases.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record CreatePurchaseRequest(
        @NotNull Long supplierId,
        @NotEmpty @Valid List<PurchaseItemRequest> items,
        LocalDateTime expectedDeliveryDate,
        String notes,
        String buyerEmail
) {}
