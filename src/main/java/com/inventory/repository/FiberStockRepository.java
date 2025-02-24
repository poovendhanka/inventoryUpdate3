package com.inventory.repository;

import com.inventory.model.FiberStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FiberStockRepository extends JpaRepository<FiberStock, Long> {
    Optional<FiberStock> findTopByOrderByUpdatedAtDesc();
} 