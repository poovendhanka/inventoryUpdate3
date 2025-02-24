package com.inventory.repository;

import com.inventory.model.ProcessedRawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessedRawMaterialRepository extends JpaRepository<ProcessedRawMaterial, Long> {
    @Query("SELECT p FROM ProcessedRawMaterial p " +
           "JOIN FETCH p.rawMaterial rm " +
           "JOIN FETCH rm.party " +
           "WHERE p.processedDate >= :startDate " +
           "AND p.processedDate < :endDate " +
           "ORDER BY p.processedDate DESC")
    List<ProcessedRawMaterial> findByProcessedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM ProcessedRawMaterial p")
    long count();
} 