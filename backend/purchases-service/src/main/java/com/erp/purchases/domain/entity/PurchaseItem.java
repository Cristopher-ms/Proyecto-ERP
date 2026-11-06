package com.erp.purchases.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", nullable = false)
    @JsonBackReference
    private PurchaseOrder purchaseOrder;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (unitCost != null && quantity != null) {
            this.subtotal = unitCost.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
