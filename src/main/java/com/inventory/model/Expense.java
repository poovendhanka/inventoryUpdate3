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

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type")
    private ExpenseType expenseType;

    private Double amount;

    @Column(length = 1000)
    private String description;

    // Fields for Electricity/Bills
    private Integer unitsConsumed;

    // Fields for Labour
    private Integer menCount;
    private Integer womenCount;
    private String shift;

    // Fields for Maintenance
    private String maintenanceType; // periodic or occasional

    // Fields for Transportation
    private Double fuelQuantity;
    private String vehicleDetails;

    // Fields for Spares/Parts
    private String partName;
    private String replacementDetails;

    // Common fields
    @Column(name = "expense_date")
    private LocalDateTime expenseDate;

    @Column(name = "entry_date")
    private LocalDateTime entryDate;
}