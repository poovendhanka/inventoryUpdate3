package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "processed_raw_materials")
public class ProcessedRawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "raw_material_id", unique = true)
    private RawMaterial rawMaterial;
    
    @Column(name = "cost_per_cft")
    private Double costPerCft;
    
    @Column(name = "total_cost")
    private Double totalCost;
    
    @Column(name = "accounts_supervisor")
    private String accountsSupervisor;
    
    @Column(name = "processed_date")
    private LocalDateTime processedDate;
    
    @PrePersist
    @PreUpdate
    public void calculateTotalCost() {
        if (costPerCft != null && rawMaterial != null && rawMaterial.getCft() != null) {
            this.totalCost = costPerCft * rawMaterial.getCft();
        }
    }
} 