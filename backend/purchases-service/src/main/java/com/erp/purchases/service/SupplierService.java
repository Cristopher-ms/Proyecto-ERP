package com.erp.purchases.service;

import com.erp.purchases.domain.entity.Supplier;
import com.erp.purchases.domain.repository.SupplierRepository;
import com.erp.purchases.dto.SupplierRequest;
import com.erp.purchases.dto.SupplierResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers(String search) {
        List<Supplier> suppliers = (search != null && !search.isBlank())
                ? supplierRepository.searchSuppliers(search)
                : supplierRepository.findAll();
        return suppliers.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        return toResponse(supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + id)));
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .companyName(request.companyName())
                .taxId(request.taxId())
                .contactName(request.contactName())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .country(request.country())
                .paymentTerms(request.paymentTerms())
                .notes(request.notes())
                .build();
        Supplier saved = supplierRepository.save(supplier);
        log.info("Proveedor creado: {} (RUC: {})", saved.getCompanyName(), saved.getTaxId());
        return toResponse(saved);
    }

    public SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(
                s.getId(),
                s.getCompanyName(),
                s.getTaxId(),
                s.getContactName(),
                s.getEmail(),
                s.getPhone(),
                s.getAddress(),
                s.getCountry(),
                s.getPaymentTerms(),
                s.getStatus(),
                s.getNotes()
        );
    }
}
