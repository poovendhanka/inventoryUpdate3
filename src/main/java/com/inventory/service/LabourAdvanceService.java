package com.inventory.service;

import com.inventory.model.Employee;
import com.inventory.model.Expense;
import com.inventory.model.ExpenseType;
import com.inventory.model.LabourAdvance;
import com.inventory.model.LabourEntry;
import com.inventory.repository.LabourAdvanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LabourAdvanceService {

    private final LabourAdvanceRepository advanceRepository;
    private final ExpenseService expenseService;

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal SETTLEMENT_THRESHOLD = new BigDecimal("0.0001");

    public LabourAdvance recordAdvance(Employee employee, Double amount, LocalDate advanceDate, String remarks) {
        log.info("Recording advance of ₹{} for employee: {}", amount, employee.getName());
        BigDecimal advanceAmount = scaleCurrency(amount != null ? BigDecimal.valueOf(amount) : ZERO);

        LabourAdvance advance = new LabourAdvance();
        advance.setEmployee(employee);
        advance.setAmount(advanceAmount);
        advance.setRemainingAmount(advanceAmount);
        advance.setAdvanceDate(advanceDate != null ? advanceDate : LocalDate.now());
        advance.setRemarks(remarks);
        advance.setSettled(false);
        
        LabourAdvance savedAdvance = advanceRepository.save(advance);
        
        // Create expense record for the advance
        createExpenseFromAdvance(savedAdvance);
        

        return savedAdvance;
    }

    public double getOutstandingAdvance(Employee employee) {
        BigDecimal total = advanceRepository.getOutstandingAdvanceTotal(employee);
        return total != null ? total.doubleValue() : 0.0;
    }

    public List<LabourAdvance> getOutstandingAdvances(Employee employee) {
        return advanceRepository.findOutstandingByEmployeeOrderByOldest(employee);
    }

    public List<LabourAdvance> getAllAdvancesByEmployee(Employee employee) {
        return advanceRepository.findByEmployeeOrderByAdvanceDateDesc(employee);
    }

    public List<LabourAdvance> getAdvancesByEmployeeAndDateRange(Employee employee, LocalDate startDate, LocalDate endDate) {
        return advanceRepository.findByEmployeeAndDateRange(employee, startDate, endDate);
    }

    public double getTotalAdvancesGivenInPeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        BigDecimal total = advanceRepository.getTotalAdvancesGivenInPeriod(employee, startDate, endDate);
        return total != null ? total.doubleValue() : 0.0;
    }

    /**
     * Applies advance adjustments to a labour entry using FIFO (First In First Out) method.
     * Returns the amount that was actually adjusted.
     */
    public double applyAdvanceAdjustment(LabourEntry entry, boolean applyAdvance, Double customDeduction) {
        if (!applyAdvance) {
            return 0.0;
        }

        double totalCost = entry.getTotalCost() != null ? entry.getTotalCost() : 0.0;
        if (totalCost <= 0) {
            return 0.0;
        }

        double requestedDeduction = (customDeduction != null && customDeduction > 0) 
            ? customDeduction 
            : totalCost;

        BigDecimal remainingToAdjust = scaleCurrency(BigDecimal.valueOf(Math.min(totalCost, requestedDeduction)));
        BigDecimal totalAdjusted = ZERO;

        List<LabourAdvance> outstandingAdvances = getOutstandingAdvances(entry.getEmployee());

        for (LabourAdvance advance : outstandingAdvances) {
            if (remainingToAdjust.compareTo(SETTLEMENT_THRESHOLD) <= 0) {
                break;
            }

            BigDecimal advanceRemaining = scaleCurrency(advance.getRemainingAmount());
            BigDecimal useAmount = advanceRemaining.min(remainingToAdjust);
            BigDecimal updatedRemaining = scaleCurrency(advanceRemaining.subtract(useAmount));
            advance.setRemainingAmount(updatedRemaining);

            if (advance.getRemainingAmount() != null && advance.getRemainingAmount().compareTo(SETTLEMENT_THRESHOLD) <= 0) {
                advance.setRemainingAmount(ZERO);
                advance.setSettled(true);
            }

            totalAdjusted = totalAdjusted.add(useAmount);
            remainingToAdjust = scaleCurrency(remainingToAdjust.subtract(useAmount));
        }

        // Save all modified advances
        if (totalAdjusted.compareTo(BigDecimal.ZERO) > 0) {
            advanceRepository.saveAll(outstandingAdvances);
            String dateRange = entry.getFromDate().equals(entry.getToDate()) 
                ? entry.getFromDate().toString()
                : entry.getFromDate().toString() + " to " + entry.getToDate().toString();
            log.info("Applied ₹{} advance adjustment for employee: {} for period: {}", 
                    totalAdjusted, entry.getEmployee().getName(), dateRange);
        }

        return totalAdjusted.doubleValue();
    }

    /**
     * Creates an Expense record from a LabourAdvance for expense reporting
     */
    private void createExpenseFromAdvance(LabourAdvance advance) {
        try {
            Employee employee = advance.getEmployee();
            if (employee == null) {
                log.warn("Cannot create expense for advance {}: employee is null", advance.getId());
                return;
            }

            Expense expense = new Expense();
            expense.setExpenseType(ExpenseType.LABOUR);
            expense.setAmount(advance.getAmount() != null ? advance.getAmount().doubleValue() : null);
            
            // Create description
            String description = String.format("Advance for %s", employee.getName());
            if (advance.getRemarks() != null && !advance.getRemarks().trim().isEmpty()) {
                description += " - " + advance.getRemarks();
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
            
            // Shift is not applicable for advances
            expense.setShift(null);
            
            // Set expense date to the advance date
            if (advance.getAdvanceDate() != null) {
                expense.setExpenseDate(advance.getAdvanceDate().atStartOfDay());
            } else {
                expense.setExpenseDate(LocalDateTime.now());
            }
            
            expense.setEntryDate(advance.getEntryDate() != null ? advance.getEntryDate() : LocalDateTime.now());
            
            expenseService.saveExpense(expense);
            log.info("Created expense record for advance {}: ₹{}", advance.getId(), advance.getAmount());
        } catch (Exception e) {
            log.error("Error creating expense record for advance {}", advance.getId(), e);
            // Don't throw exception - we don't want to fail the advance save if expense creation fails
        }
    }

    private BigDecimal scaleCurrency(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

