package com.inventory.repository;

import com.inventory.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findByGender(String gender);
    
    boolean existsByAadharNumber(String aadharNumber);
} 