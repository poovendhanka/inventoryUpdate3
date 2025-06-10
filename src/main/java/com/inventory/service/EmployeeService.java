package com.inventory.service;

import com.inventory.model.Employee;
import com.inventory.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public List<Employee> getEmployeesByGender(String gender) {
        return employeeRepository.findByGender(gender);
    }
    
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
    
    public boolean existsByAadharNumber(String aadharNumber) {
        return employeeRepository.existsByAadharNumber(aadharNumber);
    }
    
    public boolean isAadharNumberUnique(String aadharNumber, Long employeeId) {
        Employee existingEmployee = employeeRepository.findAll().stream()
            .filter(emp -> emp.getAadharNumber().equals(aadharNumber))
            .findFirst()
            .orElse(null);
        
        if (existingEmployee == null) {
            return true;
        }
        
        return existingEmployee.getId().equals(employeeId);
    }
} 