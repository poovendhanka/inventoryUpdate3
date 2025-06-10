package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "labour_entries")
public class LabourEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Work date is required")
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @NotNull(message = "Cost per hour is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost per hour must be greater than 0")
    @Column(name = "cost_per_hour", nullable = false)
    private Double costPerHour;

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hours worked must be greater than 0")
    @DecimalMax(value = "24.0", message = "Hours worked cannot exceed 24 hours")
    @Column(name = "hours_worked", nullable = false)
    private Double hoursWorked;

    @NotBlank(message = "Shift is required")
    @Column(name = "shift", nullable = false)
    private String shift;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @PrePersist
    @PreUpdate
    private void calculateTotalCost() {
        if (costPerHour != null && hoursWorked != null) {
            this.totalCost = costPerHour * hoursWorked;
        }
        if (entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }
    }
} 