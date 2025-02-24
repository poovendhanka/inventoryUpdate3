package com.inventory.repository;

import com.inventory.model.WhiteFiberStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WhiteFiberStockRepository extends JpaRepository<WhiteFiberStock, Long> {
    Optional<WhiteFiberStock> findTopByOrderByUpdatedAtDesc();
}