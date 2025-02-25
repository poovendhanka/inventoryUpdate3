package com.inventory.repository;

import com.inventory.model.BlockProduction;
import com.inventory.model.PithType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BlockProductionRepository extends JpaRepository<BlockProduction, Long> {
    List<BlockProduction> findTop10ByOrderByProductionTimeDesc();

    @Query("SELECT SUM(b.blocksProduced) FROM BlockProduction b")
    Integer getTotalBlocksProduced();

    @Query("SELECT SUM(b.blocksProduced) FROM BlockProduction b WHERE b.pithType = :pithType")
    Integer getTotalBlocksProducedByType(@Param("pithType") PithType pithType);
}