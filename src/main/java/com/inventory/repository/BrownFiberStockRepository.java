package com.inventory.repository;

import com.inventory.model.BrownFiberStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrownFiberStockRepository extends JpaRepository<BrownFiberStock, Long> {
    Optional<BrownFiberStock> findTopByOrderByUpdatedAtDesc();
}