package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @NotNull(message = "From date is required")
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @NotNull(message = "Cost per hour is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost per hour must be greater than 0")
    @Column(name = "cost_per_hour", nullable = false)
    private Double costPerHour;

    @NotNull(message = "Hours worked is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hours worked must be greater than 0")
    @Column(name = "hours_worked", nullable = false)
    private Double hoursWorked;

    @NotBlank(message = "Shift is required")
    @Column(name = "shift", nullable = false)
    private String shift;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "advance_adjustment", nullable = false, precision = 12, scale = 2)
    private BigDecimal advanceAdjustment = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "net_payable", nullable = false, precision = 12, scale = 2)
    private BigDecimal netPayable = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @PrePersist
    @PreUpdate
    private void calculateTotalCost() {
        // Validate date range
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("To date must be greater than or equal to From date");
        }
        
        if (costPerHour != null && hoursWorked != null) {
            this.totalCost = costPerHour * hoursWorked;
        }
        if (entryDate == null) {
            this.entryDate = LocalDateTime.now();
        }
        // Calculate net payable if not already set (ensures it never goes negative)
        if (netPayable == null || netPayable.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal total = totalCost != null
                    ? BigDecimal.valueOf(totalCost).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal adjustment = advanceAdjustment != null
                    ? advanceAdjustment
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            BigDecimal calculated = total.subtract(adjustment);
            if (calculated.compareTo(BigDecimal.ZERO) < 0) {
                calculated = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
            this.netPayable = calculated.setScale(2, RoundingMode.HALF_UP);
        }
        if (advanceAdjustment == null) {
            this.advanceAdjustment = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
} 