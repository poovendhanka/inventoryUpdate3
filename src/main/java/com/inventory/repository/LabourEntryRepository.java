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

    List<LabourEntry> findByWorkDateBetweenOrderByWorkDateDescEntryDateDesc(LocalDate startDate, LocalDate endDate);

    List<LabourEntry> findByEmployeeOrderByWorkDateDescEntryDateDesc(Employee employee);

    List<LabourEntry> findByWorkDateOrderByEntryDateDesc(LocalDate workDate);

    @Query("SELECT le FROM LabourEntry le ORDER BY le.workDate DESC, le.entryDate DESC")
    List<LabourEntry> findAllOrderByWorkDateDescEntryDateDesc();

    @Query("SELECT SUM(le.totalCost) FROM LabourEntry le WHERE le.workDate BETWEEN :startDate AND :endDate")
    Double getTotalCostByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(le.totalCost) FROM LabourEntry le WHERE le.workDate = :date")
    Double getTotalCostByDate(@Param("date") LocalDate date);

    boolean existsByEmployeeAndWorkDate(Employee employee, LocalDate workDate);
} 