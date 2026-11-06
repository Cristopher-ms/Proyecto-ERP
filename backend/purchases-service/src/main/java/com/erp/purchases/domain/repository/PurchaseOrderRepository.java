package com.erp.purchases.domain.repository;

import com.erp.purchases.domain.entity.PurchaseOrder;
import com.erp.purchases.domain.enums.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT p FROM PurchaseOrder p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:search IS NULL OR LOWER(p.purchaseNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.supplier.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PurchaseOrder> searchOrders(@Param("status") PurchaseStatus status,
                                     @Param("search") String search,
                                     Pageable pageable);

    @Query("SELECT COUNT(p) FROM PurchaseOrder p")
    long countTotal();

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM PurchaseOrder p WHERE p.status = 'RECEIVED'")
    BigDecimal sumTotalPurchases();

    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status = 'DRAFT' OR p.status = 'APPROVED'")
    long countPending();

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM PurchaseOrder p WHERE p.status = 'RECEIVED' AND p.receivedDate >= :since")
    BigDecimal sumPurchasesSince(@Param("since") LocalDateTime since);
}
