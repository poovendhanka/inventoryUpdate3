package com.inventory.service;

import com.inventory.model.LabourEntry;
import com.inventory.model.Employee;
import com.inventory.dto.EmployeeWorkReportDTO;
import com.inventory.repository.LabourEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LabourEntryService {

    private final LabourEntryRepository labourEntryRepository;

    public List<LabourEntry> getAllLabourEntries() {
        return labourEntryRepository.findAllOrderByWorkDateDescEntryDateDesc();
    }

    public List<LabourEntry> getLabourEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return labourEntryRepository.findByWorkDateBetweenOrderByWorkDateDescEntryDateDesc(startDate, endDate);
    }

    public List<LabourEntry> getLabourEntriesByDate(LocalDate date) {
        return labourEntryRepository.findByWorkDateOrderByEntryDateDesc(date);
    }

    public List<LabourEntry> getLabourEntriesByEmployee(Employee employee) {
        return labourEntryRepository.findByEmployeeOrderByWorkDateDescEntryDateDesc(employee);
    }

    public Optional<LabourEntry> getLabourEntryById(Long id) {
        return labourEntryRepository.findById(id);
    }

    public LabourEntry saveLabourEntry(LabourEntry labourEntry) {
        log.info("Saving labour entry for employee: {} on date: {}", 
                labourEntry.getEmployee().getName(), labourEntry.getWorkDate());
        
        // Check if an entry already exists for this employee on this date
        if (labourEntry.getId() == null && 
            labourEntryRepository.existsByEmployeeAndWorkDate(labourEntry.getEmployee(), labourEntry.getWorkDate())) {
            throw new IllegalArgumentException("A labour entry already exists for employee " + 
                labourEntry.getEmployee().getName() + " on " + labourEntry.getWorkDate());
        }
        
        return labourEntryRepository.save(labourEntry);
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
        return labourEntryRepository.existsByEmployeeAndWorkDate(employee, date);
    }

    public List<LabourEntry> getRecentLabourEntries(int limit) {
        List<LabourEntry> allEntries = labourEntryRepository.findAllOrderByWorkDateDescEntryDateDesc();
        return allEntries.size() > limit ? allEntries.subList(0, limit) : allEntries;
    }

    // Employee Work Report Methods
    public List<LabourEntry> getLabourEntriesByEmployeeAndDateRange(Employee employee, LocalDate startDate, LocalDate endDate) {
        return labourEntryRepository.findByEmployeeAndWorkDateBetweenOrderByWorkDateDesc(employee, startDate, endDate);
    }
    
    public EmployeeWorkReportDTO generateEmployeeWorkReport(Employee employee, LocalDate startDate, LocalDate endDate) {
        log.info("Generating work report for employee: {} from {} to {}", 
                employee.getName(), startDate, endDate);
        
        List<LabourEntry> entries = getLabourEntriesByEmployeeAndDateRange(employee, startDate, endDate);
        
        if (entries.isEmpty()) {
            return new EmployeeWorkReportDTO(0, 0.0, 0.0, 0.0, entries);
        }
        
        // Calculate totals
        double totalHours = entries.stream()
                .mapToDouble(LabourEntry::getHoursWorked)
                .sum();
        
        double totalSalary = entries.stream()
                .mapToDouble(LabourEntry::getTotalCost)
                .sum();
        
        // Calculate unique working days
        Set<LocalDate> uniqueDates = entries.stream()
                .map(LabourEntry::getWorkDate)
                .collect(Collectors.toSet());
        int totalDays = uniqueDates.size();
        
        // Calculate average per day
        double averagePerDay = totalDays > 0 ? totalSalary / totalDays : 0.0;
        
        return new EmployeeWorkReportDTO(totalDays, totalHours, totalSalary, averagePerDay, entries);
    }
} 