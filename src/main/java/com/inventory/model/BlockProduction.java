package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "block_production")
@NoArgsConstructor
public class BlockProduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pith_type", nullable = false)
    private PithType pithType = PithType.NORMAL; // Default to normal

    @Column(name = "blocks_produced")
    private Integer blocksProduced;

    @Column(name = "pith_quantity_used")
    private Double pithQuantityUsed;

    @Column(name = "production_time")
    private LocalDateTime productionTime;

    @Column(name = "system_time")
    private LocalDateTime systemTime;

    @Column(name = "supervisor_name")
    private String supervisorName;

    @PrePersist
    void prePersist() {
        this.systemTime = LocalDateTime.now();
        this.pithQuantityUsed = this.blocksProduced * 5.0;
    }
}