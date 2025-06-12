package com.inventory.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "product_cost")
@NoArgsConstructor
public class ProductCost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double blockNormalCost;
    private Double blockLowCost;
    private Double pithNormalCost;
    private Double pithLowCost;
    private Double fiberWhiteCost;
    private Double fiberBrownCost;
} 