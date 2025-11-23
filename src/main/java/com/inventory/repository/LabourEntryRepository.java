package com.inventory.repository;

import com.inventory.model.LabourEntry;
import com.inventory.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LabourEntryRepository extends JpaRepository<LabourEntry, Long> {

    // Find entries where the entry's date range overlaps with the given date range
    @Query("SELECT le FROM LabourEntry le WHERE (le.fromDate <= :endDate AND le.toDate >= :startDate) ORDER BY le.fromDate DESC, le.entryDate DESC")
    List<LabourEntry> findByDateRangeOverlapOrderByFromDateDescEntryDateDesc(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT le FROM LabourEntry le ORDER BY le.fromDate DESC, le.entryDate DESC")
    List<LabourEntry> findAllOrderByFromDateDescEntryDateDesc();

    @Query("SELECT le FROM LabourEntry le WHERE le.employee = :employee ORDER BY le.fromDate DESC, le.entryDate DESC")
    List<LabourEntry> findByEmployeeOrderByFromDateDescEntryDateDesc(@Param("employee") Employee employee);

    // Find entries for employee where the entry's date range overlaps with the given date range
    @Query("SELECT le FROM LabourEntry le WHERE le.employee = :employee AND (le.fromDate <= :endDate AND le.toDate >= :startDate) ORDER BY le.fromDate DESC")
    List<LabourEntry> findByEmployeeAndDateRangeOverlapOrderByFromDateDesc(@Param("employee") Employee employee, 
                                                                           @Param("startDate") LocalDate startDate, 
                                                                           @Param("endDate") LocalDate endDate);

    // Find entries that overlap with a specific date
    @Query("SELECT le FROM LabourEntry le WHERE le.fromDate <= :date AND le.toDate >= :date ORDER BY le.entryDate DESC")
    List<LabourEntry> findByDateOverlapOrderByEntryDateDesc(@Param("date") LocalDate date);

    // Sum queries - entries where date range overlaps with query range
    @Query("SELECT SUM(le.totalCost) FROM LabourEntry le WHERE (le.fromDate <= :endDate AND le.toDate >= :startDate)")
    Double getTotalCostByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(le.totalCost) FROM LabourEntry le WHERE le.fromDate <= :date AND le.toDate >= :date")
    Double getTotalCostByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(le.advanceAdjustment) FROM LabourEntry le WHERE le.employee = :employee AND (le.fromDate <= :endDate AND le.toDate >= :startDate)")
    Double getTotalAdvanceAdjustmentByEmployeeAndDateRange(@Param("employee") Employee employee, 
                                                            @Param("startDate") LocalDate startDate, 
                                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(le.advanceAdjustment) FROM LabourEntry le WHERE (le.fromDate <= :endDate AND le.toDate >= :startDate)")
    Double getTotalAdvanceAdjustmentByDateRange(@Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(le.netPayable) FROM LabourEntry le WHERE (le.fromDate <= :endDate AND le.toDate >= :startDate)")
    Double getTotalNetPayableByDateRange(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);

    // Check if employee has overlapping date range entry
    @Query("SELECT COUNT(le) > 0 FROM LabourEntry le WHERE le.employee = :employee AND le.id != COALESCE(:excludeId, -1) AND (le.fromDate <= :toDate AND le.toDate >= :fromDate)")
    boolean existsByEmployeeAndDateRangeOverlap(@Param("employee") Employee employee, 
                                                 @Param("fromDate") LocalDate fromDate, 
                                                 @Param("toDate") LocalDate toDate,
                                                 @Param("excludeId") Long excludeId);
} 