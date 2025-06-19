package com.inventory.repository;

import com.inventory.model.ManualBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManualBillRepository extends JpaRepository<ManualBill, Long> {
    
    Optional<ManualBill> findByBillNumber(String billNumber);
    
    List<ManualBill> findByBillDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ManualBill> findByCustomerNameContainingIgnoreCase(String customerName);
    
    @Query("SELECT m FROM ManualBill m ORDER BY m.billDate DESC, m.id DESC")
    List<ManualBill> findAllOrderByBillDateDesc();
    
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(m.billNumber, 3) AS int)), 0) FROM ManualBill m WHERE m.billNumber LIKE 'MB%'")
    Integer findLastBillNumber();
} 