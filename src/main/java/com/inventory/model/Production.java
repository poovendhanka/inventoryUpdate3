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

    @Column(name = "num_bales")
    private Integer numBales = 18;

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

    @Column(name = "fiber_bales")
    private Integer fiberBales;

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

    @PrePersist
    void prePersist() {
        this.systemTime = LocalDateTime.now();
        this.fiberBales = this.numBales;

        // Set fiber type based on husk type
        if (this.huskType == null) {
            throw new RuntimeException("Husk type must be specified");
        }
        this.fiberType = (this.huskType == HuskType.GREEN) ? FiberType.WHITE : FiberType.BROWN;

        if (this.productionDate == null) {
            this.productionDate = LocalDate.now();
        }

        if (this.batchCompletionTime == null) {
            this.batchCompletionTime = LocalDateTime.now();
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