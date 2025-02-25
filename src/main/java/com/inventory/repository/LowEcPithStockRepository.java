package com.inventory.repository;

import com.inventory.model.LowEcPithStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LowEcPithStockRepository extends JpaRepository<LowEcPithStock, Long> {
    Optional<LowEcPithStock> findTopByOrderByUpdatedAtDesc();
}