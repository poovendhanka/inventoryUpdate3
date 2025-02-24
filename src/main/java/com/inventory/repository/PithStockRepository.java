package com.inventory.repository;

import com.inventory.model.PithStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PithStockRepository extends JpaRepository<PithStock, Long> {
    Optional<PithStock> findTopByOrderByUpdatedAtDesc();
} 