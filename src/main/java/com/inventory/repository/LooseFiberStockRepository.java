package com.inventory.repository;

import com.inventory.model.LooseFiberStock;
import com.inventory.model.FiberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LooseFiberStockRepository extends JpaRepository<LooseFiberStock, Long> {
    
    Optional<LooseFiberStock> findTopByFiberTypeOrderByUpdatedAtDesc(FiberType fiberType);
    
    @Query("SELECT SUM(l.quantity) FROM LooseFiberStock l WHERE l.fiberType = :fiberType")
    Double getTotalStockByFiberType(@Param("fiberType") FiberType fiberType);
} 