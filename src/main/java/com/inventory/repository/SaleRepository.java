package com.inventory.repository;

import com.inventory.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findTop10ByOrderBySaleDateDesc();
    long count();
    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime startDate, LocalDateTime endDate);
} 