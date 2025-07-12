package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "fiber_production")
@NoArgsConstructor
public class FiberProduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "production_time")
    private LocalDateTime productionTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiber_type")
    private FiberType fiberType;

    @Column(name = "loose_fiber_consumed")
    private Double looseFiberConsumed;

    @Column(name = "bales_produced")
    private Integer balesProduced;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.productionTime == null) {
            this.productionTime = LocalDateTime.now();
        }
        if (this.productionDate == null) {
            this.productionDate = LocalDate.now();
        }
    }
} 