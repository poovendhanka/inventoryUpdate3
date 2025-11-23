package com.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "labour_advances")
public class LabourAdvance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false)
    private Double amount;

    @NotNull
    @Column(name = "remaining_amount", nullable = false)
    private Double remainingAmount;

    @NotNull(message = "Advance date is required")
    @Column(name = "advance_date", nullable = false)
    private LocalDate advanceDate;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private Boolean settled = false;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @PrePersist
    public void prePersist() {
        if (remainingAmount == null) {
            remainingAmount = amount;
        }
        if (remainingAmount != null && remainingAmount <= 0.0001) {
            settled = true;
            remainingAmount = 0.0;
        }
        if (entryDate == null) {
            entryDate = LocalDateTime.now();
        }
        if (advanceDate == null) {
            advanceDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (remainingAmount != null && remainingAmount <= 0.0001) {
            settled = true;
            remainingAmount = 0.0;
        }
    }
}

