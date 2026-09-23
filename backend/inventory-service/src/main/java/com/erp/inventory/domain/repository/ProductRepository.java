package com.erp.inventory.domain.repository;

import com.erp.inventory.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                             OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:lowStockOnly = false OR p.stockQuantity <= p.minStockAlert)
    """)
    Page<Product> findProductsWithFilters(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            @Param("lowStockOnly") boolean lowStockOnly,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockQuantity <= p.minStockAlert")
    long countLowStockProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true")
    long countActiveProducts();

    @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM Product p WHERE p.active = true")
    long sumTotalStockQuantity();

    @Query("SELECT COALESCE(SUM(p.stockQuantity * p.price), 0) FROM Product p WHERE p.active = true")
    BigDecimal calculateTotalStockValuation();

    List<Product> findTop5ByActiveTrueAndStockQuantityLessThanEqualOrderByStockQuantityAsc(Integer threshold);
}
