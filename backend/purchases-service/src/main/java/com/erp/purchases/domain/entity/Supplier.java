package com.erp.purchases.domain.entity;

import com.erp.purchases.domain.enums.PaymentTerms;
import com.erp.purchases.domain.enums.SupplierStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, unique = true, length = 30)
    private String taxId;

    @Column(length = 100)
    private String contactName;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 250)
    private String address;

    @Column(length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = SupplierStatus.ACTIVE;
        if (this.paymentTerms == null) this.paymentTerms = PaymentTerms.NET_30;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
