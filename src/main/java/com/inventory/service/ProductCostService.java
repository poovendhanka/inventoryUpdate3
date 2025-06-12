package com.inventory.service;

import com.inventory.model.ProductCost;
import com.inventory.repository.ProductCostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductCostService {
    private final ProductCostRepository productCostRepository;

    public ProductCost getCurrentCost() {
        ProductCost cost = productCostRepository.findTopByOrderByIdDesc();
        return cost != null ? cost : new ProductCost();
    }

    @Transactional
    public void saveCost(ProductCost productCost) {
        productCostRepository.save(productCost);
    }
} 