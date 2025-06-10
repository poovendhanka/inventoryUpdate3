package com.inventory.service;

import com.inventory.model.Expense;
import com.inventory.model.ExpenseType;
import com.inventory.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public Expense saveExpense(Expense expense) {
        if (expense.getEntryDate() == null) {
            expense.setEntryDate(LocalDateTime.now());
        }

        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDateTime.now());
        }

        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getExpensesByType(ExpenseType expenseType) {
        return expenseRepository.findByExpenseType(expenseType);
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return expenseRepository.findByExpenseDateBetween(startDateTime, endDateTime);
    }

    public List<Expense> getExpensesByTypeAndDateRange(ExpenseType expenseType, LocalDate startDate,
            LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return expenseRepository.findByExpenseTypeAndExpenseDateBetween(expenseType, startDateTime, endDateTime);
    }

    public Double getTotalExpenseAmountByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Double total = expenseRepository.sumAmountByDateBetween(startDateTime, endDateTime);
        return total != null ? total : 0.0;
    }

    public Double getTotalExpenseAmountByTypeAndDateRange(ExpenseType expenseType, LocalDate startDate,
            LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Double total = expenseRepository.sumAmountByExpenseTypeAndDateBetween(expenseType, startDateTime, endDateTime);
        return total != null ? total : 0.0;
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }
}