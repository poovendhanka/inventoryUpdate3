package com.inventory.repository;

import com.inventory.model.Employee;
import com.inventory.model.LabourAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LabourAdvanceRepository extends JpaRepository<LabourAdvance, Long> {
    
    @Query("SELECT a FROM LabourAdvance a WHERE a.employee = :employee AND a.settled = false ORDER BY a.advanceDate ASC, a.id ASC")
    List<LabourAdvance> findOutstandingByEmployeeOrderByOldest(@Param("employee") Employee employee);
    
    @Query("SELECT a FROM LabourAdvance a WHERE a.employee = :employee ORDER BY a.advanceDate DESC, a.id DESC")
    List<LabourAdvance> findByEmployeeOrderByAdvanceDateDesc(@Param("employee") Employee employee);
    
    @Query("SELECT a FROM LabourAdvance a WHERE a.employee = :employee AND a.advanceDate BETWEEN :startDate AND :endDate ORDER BY a.advanceDate DESC")
    List<LabourAdvance> findByEmployeeAndDateRange(@Param("employee") Employee employee, 
                                                    @Param("startDate") LocalDate startDate, 
                                                    @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COALESCE(SUM(a.remainingAmount), 0) FROM LabourAdvance a WHERE a.employee = :employee AND a.settled = false")
    java.math.BigDecimal getOutstandingAdvanceTotal(@Param("employee") Employee employee);
    
    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM LabourAdvance a WHERE a.employee = :employee AND a.advanceDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalAdvancesGivenInPeriod(@Param("employee") Employee employee, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
}

