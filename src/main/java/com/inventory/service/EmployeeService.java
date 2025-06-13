package com.inventory.service;

import com.inventory.model.Employee;
import com.inventory.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // Active employee methods
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findAllActive();
    }
    
    public List<Employee> getActiveEmployeesByGender(String gender) {
        return employeeRepository.findActiveByGender(gender);
    }
    
    public Optional<Employee> getActiveEmployeeById(Long id) {
        return employeeRepository.findActiveById(id);
    }
    
    // Deleted employee methods (Old Employees)
    public List<Employee> getAllDeletedEmployees() {
        return employeeRepository.findAllDeleted();
    }
    
    public List<Employee> getDeletedEmployeesByGender(String gender) {
        return employeeRepository.findDeletedByGender(gender);
    }
    
    // Soft delete operations
    public void softDeleteEmployee(Long id, String deletedBy) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.softDelete(deletedBy != null ? deletedBy : "System");
            employeeRepository.save(employee);
        }
    }
    
    public void restoreEmployee(Long id) {
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.restore();
            employeeRepository.save(employee);
        }
    }
    
    // CRUD operations
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    // Validation methods
    public boolean existsByAadharNumberAmongActive(String aadharNumber) {
        return employeeRepository.existsByAadharNumberAndNotDeleted(aadharNumber);
    }
    
    public boolean isAadharNumberUniqueAmongActive(String aadharNumber, Long employeeId) {
        if (employeeId == null) {
            return !existsByAadharNumberAmongActive(aadharNumber);
        }
        return !employeeRepository.existsByAadharNumberAndNotDeletedExcludingId(aadharNumber, employeeId);
    }
    
    // Get any employee by ID (including deleted ones)
    public Optional<Employee> getAnyEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    // Legacy methods for backward compatibility (now return active employees only)
    public List<Employee> getAllEmployees() {
        return getAllActiveEmployees();
    }
    
    public List<Employee> getEmployeesByGender(String gender) {
        return getActiveEmployeesByGender(gender);
    }
    
    public Optional<Employee> getEmployeeById(Long id) {
        return getActiveEmployeeById(id);
    }
    
    public void deleteEmployee(Long id) {
        softDeleteEmployee(id, "System");
    }
    
    public boolean existsByAadharNumber(String aadharNumber) {
        return existsByAadharNumberAmongActive(aadharNumber);
    }
    
    public boolean isAadharNumberUnique(String aadharNumber, Long employeeId) {
        return isAadharNumberUniqueAmongActive(aadharNumber, employeeId);
    }
} 