package com.inventory.service;

import com.inventory.model.LabourEntry;
import com.inventory.model.Employee;
import com.inventory.model.Expense;
import com.inventory.model.ExpenseType;
import com.inventory.dto.EmployeeWorkReportDTO;
import com.inventory.repository.LabourEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LabourEntryService {

    private final LabourEntryRepository labourEntryRepository;
    private final LabourAdvanceService labourAdvanceService;
    private final ExpenseService expenseService;

    public List<LabourEntry> getAllLabourEntries() {
        return labourEntryRepository.findAllOrderByFromDateDescEntryDateDesc();
    }

    public List<LabourEntry> getLabourEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return labourEntryRepository.findByDateRangeOverlapOrderByFromDateDescEntryDateDesc(startDate, endDate);
    }

    public List<LabourEntry> getLabourEntriesByDate(LocalDate date) {
        return labourEntryRepository.findByDateOverlapOrderByEntryDateDesc(date);
    }

    public List<LabourEntry> getLabourEntriesByEmployee(Employee employee) {
        return labourEntryRepository.findByEmployeeOrderByFromDateDescEntryDateDesc(employee);
    }

    public Optional<LabourEntry> getLabourEntryById(Long id) {
        return labourEntryRepository.findById(id);
    }

    public LabourEntry saveLabourEntry(LabourEntry labourEntry) {
        // Validate date range
        validateDateRange(labourEntry.getFromDate(), labourEntry.getToDate());
        
        log.info("Saving labour entry for employee: {} from {} to {}", 
                labourEntry.getEmployee().getName(), labourEntry.getFromDate(), labourEntry.getToDate());
        
        // Check if an entry with overlapping date range already exists for this employee
        if (labourEntryRepository.existsByEmployeeAndDateRangeOverlap(
                labourEntry.getEmployee(), 
                labourEntry.getFromDate(), 
                labourEntry.getToDate(),
                labourEntry.getId())) {
            throw new IllegalArgumentException("A labour entry with overlapping date range already exists for employee " + 
                labourEntry.getEmployee().getName() + " for the period " + labourEntry.getFromDate() + " to " + labourEntry.getToDate());
        }
        
        LabourEntry savedEntry = labourEntryRepository.save(labourEntry);
        
        // Create expense record for the labor entry
        createExpenseFromLabourEntry(savedEntry);
        
        return savedEntry;
    }

    public LabourEntry saveLabourEntryWithAdvance(LabourEntry labourEntry, boolean applyAdvance, Double customDeduction) {
        // Validate date range
        validateDateRange(labourEntry.getFromDate(), labourEntry.getToDate());
        
        log.info("Saving labour entry with advance adjustment for employee: {} from {} to {}", 
                labourEntry.getEmployee().getName(), labourEntry.getFromDate(), labourEntry.getToDate());
        
        // Check if an entry with overlapping date range already exists for this employee
        if (labourEntryRepository.existsByEmployeeAndDateRangeOverlap(
                labourEntry.getEmployee(), 
                labourEntry.getFromDate(), 
                labourEntry.getToDate(),
                labourEntry.getId())) {
            throw new IllegalArgumentException("A labour entry with overlapping date range already exists for employee " + 
                labourEntry.getEmployee().getName() + " for the period " + labourEntry.getFromDate() + " to " + labourEntry.getToDate());
        }
        
        // Apply advance adjustment if requested
        double adjustmentAmount = labourAdvanceService.applyAdvanceAdjustment(labourEntry, applyAdvance, customDeduction);
        labourEntry.setAdvanceAdjustment(java.math.BigDecimal.valueOf(adjustmentAmount));
        
        // Calculate net payable
        double totalCost = labourEntry.getTotalCost() != null ? labourEntry.getTotalCost() : 0.0;
        double netPayable = Math.max(0.0, totalCost - adjustmentAmount);
        labourEntry.setNetPayable(java.math.BigDecimal.valueOf(netPayable));
        
        LabourEntry savedEntry = labourEntryRepository.save(labourEntry);
        
        // Create expense record for the labor entry
        createExpenseFromLabourEntry(savedEntry);
        
        return savedEntry;
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("From date and To date are required");
        }
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("To date must be greater than or equal to From date");
        }
    }

    public LabourEntry updateLabourEntry(LabourEntry labourEntry) {
        log.info("Updating labour entry with ID: {}", labourEntry.getId());
        return labourEntryRepository.save(labourEntry);
    }

    public void deleteLabourEntry(Long id) {
        log.info("Deleting labour entry with ID: {}", id);
        labourEntryRepository.deleteById(id);
    }

    public Double getTotalCostByDateRange(LocalDate startDate, LocalDate endDate) {
        Double total = labourEntryRepository.getTotalCostByDateRange(startDate, endDate);
        return total != null ? total : 0.0;
    }

    public Double getTotalCostByDate(LocalDate date) {
        Double total = labourEntryRepository.getTotalCostByDate(date);
        return total != null ? total : 0.0;
    }

    public boolean hasLabourEntryForEmployeeOnDate(Employee employee, LocalDate date) {
        // Check if employee has any entry that overlaps with the given date
        List<LabourEntry> entries = labourEntryRepository.findByEmployeeOrderByFromDateDescEntryDateDesc(employee);
        return entries.stream().anyMatch(entry -> 
            !entry.getFromDate().isAfter(date) && !entry.getToDate().isBefore(date));
    }

    public List<LabourEntry> getRecentLabourEntries(int limit) {
        List<LabourEntry> allEntries = labourEntryRepository.findAllOrderByFromDateDescEntryDateDesc();
        return allEntries.size() > limit ? allEntries.subList(0, limit) : allEntries;
    }

    // Employee Work Report Methods
    public List<LabourEntry> getLabourEntriesByEmployeeAndDateRange(Employee employee, LocalDate startDate, LocalDate endDate) {
        return labourEntryRepository.findByEmployeeAndDateRangeOverlapOrderByFromDateDesc(employee, startDate, endDate);
    }
    
    public EmployeeWorkReportDTO generateEmployeeWorkReport(Employee employee, LocalDate startDate, LocalDate endDate) {
        log.info("Generating work report for employee: {} from {} to {}", 
                employee.getName(), startDate, endDate);
        
        List<LabourEntry> entries = getLabourEntriesByEmployeeAndDateRange(employee, startDate, endDate);
        
        // Calculate advance information
        double openingAdvance = labourAdvanceService.getOutstandingAdvance(employee);
        // Get advances given before the period to calculate opening balance
        // For simplicity, we'll use current outstanding as opening (can be enhanced later)
        double advancesGiven = labourAdvanceService.getTotalAdvancesGivenInPeriod(employee, startDate, endDate);
        double totalAdvanceAdjustment = getTotalAdvanceAdjustmentByEmployeeAndDateRange(employee, startDate, endDate);
        double closingAdvance = openingAdvance + advancesGiven - totalAdvanceAdjustment;
        
        if (entries.isEmpty()) {
            EmployeeWorkReportDTO dto = new EmployeeWorkReportDTO(0, 0.0, 0.0, 0.0, 0.0, 0.0, openingAdvance, advancesGiven, closingAdvance, entries);
            return dto;
        }
        
        // Calculate totals
        double totalHours = entries.stream()
                .mapToDouble(LabourEntry::getHoursWorked)
                .sum();
        
        double totalSalary = entries.stream()
                .mapToDouble(LabourEntry::getTotalCost)
                .sum();
        
        double totalNetPayable = entries.stream()
                .mapToDouble(entry -> entry.getNetPayable() != null ? entry.getNetPayable().doubleValue() : 0.0)
                .sum();
        
        // Calculate unique working days - sum of days in all date ranges
        int totalDays = (int) entries.stream()
                .mapToLong(entry -> {
                    if (entry.getFromDate() != null && entry.getToDate() != null) {
                        return java.time.temporal.ChronoUnit.DAYS.between(entry.getFromDate(), entry.getToDate()) + 1;
                    }
                    return 0;
                })
                .sum();
        
        // Calculate average per day
        double averagePerDay = totalDays > 0 ? totalSalary / totalDays : 0.0;
        
        return new EmployeeWorkReportDTO(totalDays, totalHours, totalSalary, averagePerDay, 
                totalAdvanceAdjustment, totalNetPayable, openingAdvance, advancesGiven, closingAdvance, entries);
    }

    public double getTotalAdvanceAdjustmentByEmployeeAndDateRange(Employee employee, LocalDate startDate, LocalDate endDate) {
        java.math.BigDecimal total = labourEntryRepository.getTotalAdvanceAdjustmentByEmployeeAndDateRange(employee, startDate, endDate);
        return total != null ? total.doubleValue() : 0.0;
    }

    public double getTotalAdvanceAdjustmentByDateRange(LocalDate startDate, LocalDate endDate) {
        java.math.BigDecimal total = labourEntryRepository.getTotalAdvanceAdjustmentByDateRange(startDate, endDate);
        return total != null ? total.doubleValue() : 0.0;
    }

    public double getTotalNetPayableByDateRange(LocalDate startDate, LocalDate endDate) {
        java.math.BigDecimal total = labourEntryRepository.getTotalNetPayableByDateRange(startDate, endDate);
        return total != null ? total.doubleValue() : 0.0;
    }

    /**
     * Creates an Expense record from a LabourEntry for expense reporting
     */
    private void createExpenseFromLabourEntry(LabourEntry labourEntry) {
        try {
            Employee employee = labourEntry.getEmployee();
            if (employee == null) {
                log.warn("Cannot create expense for labour entry {}: employee is null", labourEntry.getId());
                return;
            }

            Expense expense = new Expense();
            expense.setExpenseType(ExpenseType.LABOUR);
            
            // Use net payable as the expense amount (actual amount paid after advance adjustment)
            double expenseAmount = labourEntry.getNetPayable() != null && labourEntry.getNetPayable().doubleValue() > 0 
                ? labourEntry.getNetPayable().doubleValue() 
                : (labourEntry.getTotalCost() != null ? labourEntry.getTotalCost() : 0.0);
            expense.setAmount(expenseAmount);
            
            // Create description
            String dateRangeStr = labourEntry.getFromDate().equals(labourEntry.getToDate())
                ? labourEntry.getFromDate().toString()
                : labourEntry.getFromDate().toString() + " to " + labourEntry.getToDate().toString();
            
            String description = String.format("Salary for %s - %s hours from %s (Shift: %s)",
                employee.getName(),
                labourEntry.getHoursWorked(),
                dateRangeStr,
                labourEntry.getShift());
            
            if (labourEntry.getAdvanceAdjustment() != null && labourEntry.getAdvanceAdjustment().doubleValue() > 0) {
                description += String.format(" | Advance Adjusted: ₹%.2f", labourEntry.getAdvanceAdjustment().doubleValue());
            }
            
            expense.setDescription(description);
            
            // Set men/women count based on employee gender
            if ("MALE".equalsIgnoreCase(employee.getGender())) {
                expense.setMenCount(1);
                expense.setWomenCount(0);
            } else if ("FEMALE".equalsIgnoreCase(employee.getGender())) {
                expense.setMenCount(0);
                expense.setWomenCount(1);
            } else {
                expense.setMenCount(0);
                expense.setWomenCount(0);
            }
            
            expense.setShift(labourEntry.getShift());
            
            // Set expense date to the fromDate (or entryDate if fromDate is null)
            if (labourEntry.getFromDate() != null) {
                expense.setExpenseDate(labourEntry.getFromDate().atStartOfDay());
            } else if (labourEntry.getEntryDate() != null) {
                expense.setExpenseDate(labourEntry.getEntryDate());
            } else {
                expense.setExpenseDate(LocalDateTime.now());
            }
            
            expense.setEntryDate(labourEntry.getEntryDate() != null ? labourEntry.getEntryDate() : LocalDateTime.now());
            
            expenseService.saveExpense(expense);
            log.info("Created expense record for labour entry {}: ₹{}", labourEntry.getId(), expenseAmount);
        } catch (Exception e) {
            log.error("Error creating expense record for labour entry {}", labourEntry.getId(), e);
            // Don't throw exception - we don't want to fail the labour entry save if expense creation fails
        }
    }
} 