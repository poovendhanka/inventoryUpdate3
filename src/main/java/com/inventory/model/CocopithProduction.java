package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cocopith_production")
@NoArgsConstructor
public class CocopithProduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pith_quantity_used")
    private Double pithQuantityUsed;

    @Column(name = "low_ec_quantity_produced")
    private Double lowEcQuantityProduced;

    @Column(name = "production_date")
    private LocalDateTime productionDate;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @PrePersist
    void prePersist() {
        if (this.productionDate == null) {
            this.productionDate = LocalDateTime.now();
        }
    }
}