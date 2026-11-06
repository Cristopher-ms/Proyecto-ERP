package com.erp.purchases.dto;

import com.erp.purchases.domain.enums.PaymentTerms;
import com.erp.purchases.domain.enums.SupplierStatus;

public record SupplierResponse(
        Long id,
        String companyName,
        String taxId,
        String contactName,
        String email,
        String phone,
        String address,
        String country,
        PaymentTerms paymentTerms,
        SupplierStatus status,
        String notes
) {}
