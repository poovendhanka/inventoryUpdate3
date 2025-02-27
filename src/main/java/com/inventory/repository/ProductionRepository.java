package com.inventory.repository;

import com.inventory.model.Production;
import com.inventory.model.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {

        @Query("SELECT p FROM Production p WHERE p.batchCompletionTime >= :startTime " +
                        "AND p.batchCompletionTime < :endTime " +
                        "AND p.shift = :shift " +
                        "ORDER BY p.batchCompletionTime ASC")
        List<Production> findByBatchCompletionTimeBetweenAndShiftOrderByBatchCompletionTimeAsc(
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime,
                        @Param("shift") ShiftType shift);

        List<Production> findByProductionDateAndShiftOrderByBatchCompletionTimeAsc(
                        LocalDate date,
                        ShiftType shift);

        @Query("SELECT p FROM Production p WHERE CAST(p.batchCompletionTime AS LocalDate) = :date " +
                        "AND p.shift = :shift ORDER BY p.batchCompletionTime ASC")
        List<Production> findByBatchCompletionTimeDate(
                        @Param("date") LocalDate date,
                        @Param("shift") ShiftType shift);

        Optional<Production> findFirstByProductionDateOrderByBatchNumberDesc(LocalDateTime date);

        List<Production> findByProductionDateOrderByBatchCompletionTimeDesc(LocalDate date);

        List<Production> findTop10ByOrderByBatchCompletionTimeDesc();
}