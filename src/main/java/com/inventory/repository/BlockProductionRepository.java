package com.inventory.repository;

import com.inventory.model.BlockProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlockProductionRepository extends JpaRepository<BlockProduction, Long> {
    List<BlockProduction> findTop10ByOrderByProductionTimeDesc();
}