package com.inventory.repository;

import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BlockProductionRepository extends JpaRepository<BlockProduction, Long> {
    List<BlockProduction> findTop10ByOrderByProductionTimeDesc();
    Page<BlockProduction> findTopByOrderByProductionTimeDesc(Pageable pageable);
    List<BlockProduction> findByProductionTimeBetweenOrderByProductionTimeDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(b.blocksProduced) FROM BlockProduction b")
    Integer getTotalBlocksProduced();

    @Query("SELECT SUM(b.blocksProduced) FROM BlockProduction b WHERE b.pithType = :pithType")
    Integer getTotalBlocksProducedByType(@Param("pithType") PithType pithType);
}