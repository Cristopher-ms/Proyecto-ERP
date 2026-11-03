package com.erp.orders.domain.repository;

import com.erp.orders.domain.entity.Order;
import com.erp.orders.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:search IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(o.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Order> searchOrders(@Param("status") OrderStatus status, @Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
    long countOrdersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'CONFIRMED' AND o.createdAt >= :since")
    BigDecimal sumTotalSalesSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(o) FROM Order o")
    long countTotalOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'CONFIRMED'")
    BigDecimal sumAllTimeSales();
}
