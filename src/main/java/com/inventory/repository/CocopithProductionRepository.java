package com.inventory.repository;

import com.inventory.model.CocopithProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CocopithProductionRepository extends JpaRepository<CocopithProduction, Long> {
    List<CocopithProduction> findByProductionDateBetweenOrderByProductionDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);
}