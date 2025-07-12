package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loose_fiber_stock")
@NoArgsConstructor
public class LooseFiberStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "quantity")
    private Double quantity = 0.0; // in kg
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fiber_type")
    private FiberType fiberType;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public LooseFiberStock(Double quantity, FiberType fiberType) {
        this.quantity = quantity;
        this.fiberType = fiberType;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 