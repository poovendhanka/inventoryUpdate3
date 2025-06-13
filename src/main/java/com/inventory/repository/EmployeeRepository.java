package com.inventory.repository;

import com.inventory.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // Find active employees only
    @Query("SELECT e FROM Employee e WHERE e.deleted = false")
    List<Employee> findAllActive();
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.gender = :gender")
    List<Employee> findActiveByGender(@Param("gender") String gender);
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.id = :id")
    Optional<Employee> findActiveById(@Param("id") Long id);
    
    // Find deleted employees (old employees)
    @Query("SELECT e FROM Employee e WHERE e.deleted = true")
    List<Employee> findAllDeleted();
    
    @Query("SELECT e FROM Employee e WHERE e.deleted = true AND e.gender = :gender")
    List<Employee> findDeletedByGender(@Param("gender") String gender);
    
    // Check if Aadhar exists among active employees only
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.aadharNumber = :aadharNumber AND e.deleted = false")
    boolean existsByAadharNumberAndNotDeleted(@Param("aadharNumber") String aadharNumber);
    
    // Check if Aadhar exists among active employees excluding specific employee
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.aadharNumber = :aadharNumber AND e.deleted = false AND e.id != :excludeId")
    boolean existsByAadharNumberAndNotDeletedExcludingId(@Param("aadharNumber") String aadharNumber, @Param("excludeId") Long excludeId);
    
    // Legacy methods for backward compatibility
    List<Employee> findByGender(String gender);
    
    boolean existsByAadharNumber(String aadharNumber);
} 