package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "raw_material_cost")
@NoArgsConstructor
public class RawMaterialCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "cost_date")
    private LocalDate date;
    
    @Column(name = "cost_per_unit")
    private Double costPerUnit;  // Cost per coconut husk
    
    @Column(name = "is_active")
    private Boolean isActive = true;
} 