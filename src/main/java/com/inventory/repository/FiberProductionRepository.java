package com.inventory.repository;

import com.inventory.model.FiberProduction;
import com.inventory.model.FiberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FiberProductionRepository extends JpaRepository<FiberProduction, Long> {

    List<FiberProduction> findByProductionDateBetweenOrderByProductionTimeDesc(
            LocalDate startDate, LocalDate endDate);

    List<FiberProduction> findByProductionDateBetweenAndFiberTypeOrderByProductionTimeDesc(
            LocalDate startDate, LocalDate endDate, FiberType fiberType);

    List<FiberProduction> findByProductionTimeGreaterThanOrderByProductionTimeDesc(
            LocalDateTime since);

    @Query("SELECT SUM(fp.balesProduced) FROM FiberProduction fp WHERE fp.productionDate BETWEEN :startDate AND :endDate")
    Integer getTotalBalesProducedBetweenDates(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(fp.looseFiberConsumed) FROM FiberProduction fp WHERE fp.productionDate BETWEEN :startDate AND :endDate")
    Double getTotalLooseFiberConsumedBetweenDates(@Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(fp.balesProduced) FROM FiberProduction fp WHERE fp.productionDate BETWEEN :startDate AND :endDate AND fp.fiberType = :fiberType")
    Integer getTotalBalesProducedByTypeAndDateRange(@Param("startDate") LocalDate startDate, 
                                                    @Param("endDate") LocalDate endDate, 
                                                    @Param("fiberType") FiberType fiberType);

    List<FiberProduction> findTop10ByOrderByProductionTimeDesc();
} 