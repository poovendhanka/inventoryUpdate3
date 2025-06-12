package com.inventory.repository;

import com.inventory.model.ProductCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCostRepository extends JpaRepository<ProductCost, Long> {
    ProductCost findTopByOrderByIdDesc();
} 