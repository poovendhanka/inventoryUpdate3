package com.inventory.repository;

import com.inventory.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByExpenseDateBetween(LocalDateTime start, LocalDateTime end);
} 