package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "expense_type")
    private String expenseType;
    private Double amount;
    private String description;
    
    @Column(name = "expense_date")
    private LocalDateTime expenseDate;
} 