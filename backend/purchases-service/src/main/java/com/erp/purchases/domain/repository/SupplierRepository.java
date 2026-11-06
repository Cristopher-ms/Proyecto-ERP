package com.erp.purchases.domain.repository;

import com.erp.purchases.domain.entity.Supplier;
import com.erp.purchases.domain.enums.SupplierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByTaxId(String taxId);

    List<Supplier> findByStatusOrderByCompanyNameAsc(SupplierStatus status);

    @Query("SELECT s FROM Supplier s WHERE s.status = 'ACTIVE' AND " +
           "(LOWER(s.companyName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "s.taxId LIKE CONCAT('%', :q, '%') OR " +
           "LOWER(s.contactName) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Supplier> searchSuppliers(@Param("q") String query);
}
