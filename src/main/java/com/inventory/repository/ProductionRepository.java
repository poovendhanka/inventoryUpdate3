package com.inventory.repository;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
    List<Production> findByProductionDateAndShiftOrderByBatchCompletionTimeAsc(
            LocalDate date,
            ShiftType shift);

    Optional<Production> findFirstByProductionDateOrderByBatchNumberDesc(LocalDateTime date);

    List<Production> findByProductionDateOrderByBatchCompletionTimeDesc(LocalDate date);

    List<Production> findTop10ByOrderByBatchCompletionTimeDesc();
}