package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "production")
@NoArgsConstructor
@Getter
@Setter
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number")
    private Integer batchNumber;

    @Column(name = "num_boxes")
    private Integer numBoxes = 1;

    @Column(name = "batch_completion_time")
    private LocalDateTime batchCompletionTime;

    @Column(name = "system_time")
    private LocalDateTime systemTime;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @Enumerated(EnumType.STRING)
    private ShiftType shift;

    @Column(name = "production_date")
    private LocalDate productionDate;

    private Double pithQuantity = 750.0;

    @Column(name = "loose_fiber_quantity")
    private Double looseFiberQuantity;

    @Column(name = "cft_consumed")
    private Double cftConsumed;

    @Transient
    private Duration timeTaken;

    @Enumerated(EnumType.STRING)
    @Column(name = "husk_type")
    private HuskType huskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiber_type")
    private FiberType fiberType;

    @Transient
    private Duration duration;

    // Production conversion constants
    private static final double CFT_TO_PITH_RATIO = 1.350; // 1 CFT = 1.350 kg pith
    private static final double CFT_TO_FIBER_RATIO = 0.650; // 1 CFT = 650g fiber

    @PrePersist
    void prePersist() {
        this.systemTime = LocalDateTime.now();

        // Set fiber type based on husk type
        if (this.huskType == null) {
            throw new RuntimeException("Husk type must be specified");
        }
        this.fiberType = (this.huskType == HuskType.GREEN) ? FiberType.WHITE : FiberType.BROWN;

        // Calculate CFT consumed based on pith quantity
        if (this.pithQuantity != null) {
            this.cftConsumed = this.pithQuantity / CFT_TO_PITH_RATIO;
            this.looseFiberQuantity = this.cftConsumed * CFT_TO_FIBER_RATIO;
        }

        if (this.productionDate == null) {
            this.productionDate = LocalDate.now();
        }

        if (this.batchCompletionTime == null) {
            this.batchCompletionTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        // Recalculate values if pith quantity changes
        if (this.pithQuantity != null) {
            this.cftConsumed = this.pithQuantity / CFT_TO_PITH_RATIO;
            this.looseFiberQuantity = this.cftConsumed * CFT_TO_FIBER_RATIO;
        }
    }

    public void calculateTimeTaken(LocalDateTime previousBatchTime) {
        if (previousBatchTime == null) {
            // For first batch of the shift
            LocalDateTime shiftStartTime;
            if (shift == ShiftType.FIRST) {
                // First shift starts at 8 AM
                shiftStartTime = batchCompletionTime.toLocalDate().atTime(8, 0);
            } else {
                // Second shift starts at 8 PM
                shiftStartTime = batchCompletionTime.toLocalDate().atTime(20, 0);
                if (batchCompletionTime.getHour() < 8) {
                    // If completion time is after midnight, shift started previous day
                    shiftStartTime = shiftStartTime.minusDays(1);
                }
            }
            this.timeTaken = Duration.between(shiftStartTime, batchCompletionTime);
        } else {
            // For subsequent batches
            this.timeTaken = Duration.between(previousBatchTime, batchCompletionTime);
        }
    }
}