package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "fiber_stock")
@NoArgsConstructor
public class FiberStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Double quantity = 0.0;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public FiberStock(Double quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }
} 