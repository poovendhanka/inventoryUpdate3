package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "low_ec_pith_stock")
@NoArgsConstructor
public class LowEcPithStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double quantity;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LowEcPithStock(Double quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }
}