package com.inventory.repository;

import com.inventory.model.HuskStock;
import com.inventory.model.HuskType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HuskStockRepository extends JpaRepository<HuskStock, Long> {
    Optional<HuskStock> findTopByHuskTypeOrderByUpdatedAtDesc(HuskType huskType);
} 