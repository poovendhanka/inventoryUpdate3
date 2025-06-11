package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "husk_stock")
@NoArgsConstructor
public class HuskStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "husk_type")
    private HuskType huskType;

    private Double quantity = 0.0; // in CFT

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HuskStock(HuskType huskType, Double quantity) {
        this.huskType = huskType;
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }
} 