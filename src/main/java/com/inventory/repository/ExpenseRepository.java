package com.inventory.repository;

import com.inventory.model.Expense;
import com.inventory.model.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByExpenseType(ExpenseType expenseType);

    List<Expense> findTop10ByExpenseTypeOrderByExpenseDateDesc(ExpenseType expenseType);

    List<Expense> findByExpenseDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Expense> findByExpenseTypeAndExpenseDateBetween(
            ExpenseType expenseType, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.expenseType = ?1 AND e.expenseDate BETWEEN ?2 AND ?3")
    Double sumAmountByExpenseTypeAndDateBetween(ExpenseType expenseType, LocalDateTime startDate,
            LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.expenseDate BETWEEN ?1 AND ?2")
    Double sumAmountByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}