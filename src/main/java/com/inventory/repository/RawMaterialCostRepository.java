package com.inventory.repository;

import com.inventory.model.RawMaterialCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RawMaterialCostRepository extends JpaRepository<RawMaterialCost, Long> {
    Optional<RawMaterialCost> findFirstByIsActiveTrueOrderByDateDesc();
} 