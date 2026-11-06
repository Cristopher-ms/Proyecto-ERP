package com.erp.purchases.dto;

import com.erp.purchases.domain.enums.PaymentTerms;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupplierRequest(
        @NotBlank String companyName,
        @NotBlank String taxId,
        String contactName,
        String email,
        String phone,
        String address,
        String country,
        @NotNull PaymentTerms paymentTerms,
        String notes
) {}
