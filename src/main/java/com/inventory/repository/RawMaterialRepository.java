package com.inventory.repository;

import com.inventory.model.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial, Long> {
    List<RawMaterial> findTop10ByOrderByLorryInTimeDesc();
    List<RawMaterial> findByAccountsProcessedFalse();
    boolean existsByReceiptNumber(String receiptNumber);
    List<RawMaterial> findByAccountsProcessedTrueAndLorryInTimeBetween(LocalDateTime start, LocalDateTime end);
} 