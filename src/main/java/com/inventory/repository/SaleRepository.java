package com.inventory.repository;

import com.inventory.model.Sale;
import com.inventory.model.PithType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findTop10ByOrderBySaleDateDesc();

    long count();

    List<Sale> findBySaleDateBetweenOrderBySaleDateDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(s.blockCount) FROM Sale s WHERE s.productType = 'BLOCK'")
    Integer getTotalBlocksSold();

    @Query("SELECT SUM(s.blockCount) FROM Sale s WHERE s.productType = 'BLOCK' AND s.pithType = :pithType")
    Integer getTotalBlocksSoldByType(@Param("pithType") PithType pithType);
}