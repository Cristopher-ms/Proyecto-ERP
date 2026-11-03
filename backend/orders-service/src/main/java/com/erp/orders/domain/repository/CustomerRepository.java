package com.erp.orders.domain.repository;

import com.erp.orders.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByDocumentNumber(String documentNumber);

    List<Customer> findByActiveTrueOrderByFirstNameAsc();

    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "c.documentNumber LIKE CONCAT('%', :q, '%'))")
    List<Customer> searchCustomers(@Param("q") String query);
}
